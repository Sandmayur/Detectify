package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.util.SafeHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fakecompanydetector.entity.CareersCache;
import com.fakecompanydetector.entity.enums.CacheStatus;
import com.fakecompanydetector.repository.CareersCacheRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(5)
public class CareersPageAnalyzer implements RiskSignalAnalyzer {

    private final SafeHttpClient httpClient;
    private final CareersCacheRepository cacheRepository;

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getResolvedDomain() != null && !ctx.getResolvedDomain().isBlank()
                && ctx.getRequest().getJobTitle() != null && !ctx.getRequest().getJobTitle().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        String baseUrl = "https://" + ctx.getResolvedDomain();
        String jobTitle = ctx.getRequest().getJobTitle().toLowerCase();

        try {
            // Check cache
            String domain = new URI(baseUrl).getHost().toLowerCase();
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            Optional<CareersCache> cached = cacheRepository.findById(new CareersCache.CareersCacheId(domain, jobTitle));
            if (cached.isPresent() && cached.get().getFetchedAt().isAfter(LocalDateTime.now().minusDays(7))) {
                CacheStatus cacheStatus = cached.get().getStatus();
                if (cacheStatus == CacheStatus.MATCHED) {
                    return buildResult(SignalType.POSITIVE, -10, "Job title was found on the official careers page.", "Cached verification.", SignalConfidence.HIGH, "Careers Cache");
                } else if (cacheStatus == CacheStatus.NOT_FOUND) {
                    return buildResult(SignalType.WARNING, 10, "Job title was not found on the official careers page.", "Cached verification.", SignalConfidence.MEDIUM, "Careers Cache");
                } else {
                    return buildUnknown("Could not verify careers page (cached result).");
                }
            }

            // Check robots.txt
            if (ctx.getIsRobotsTxtBlocked() == null) {
                ctx.setIsRobotsTxtBlocked(isBlockedByRobots(baseUrl));
            }
            if (Boolean.TRUE.equals(ctx.getIsRobotsTxtBlocked())) {
                saveCache(domain, jobTitle, CacheStatus.UNKNOWN);
                return buildUnknown("Careers search blocked by robots.txt.");
            }

            // Fetch homepage to find careers link
            if (ctx.getHomepageHtml() == null) {
                String fetchedHtml = httpClient.safeFetch(baseUrl);
                ctx.setHomepageHtml(fetchedHtml);
                ctx.setHomepageDoc(Jsoup.parse(fetchedHtml, baseUrl));
            }
            String homeHtml = ctx.getHomepageHtml();
            Document homeDoc = ctx.getHomepageDoc();
            
            String careersUrl = findCareersUrl(homeDoc, baseUrl);
            if (careersUrl == null) {
                // If not found in HTML, guess standard paths
                if (!checkPath(baseUrl, "/careers") && !checkPath(baseUrl, "/jobs")) {
                    return buildUnknown("Could not discover a careers page.");
                }
                careersUrl = baseUrl + (baseUrl.endsWith("/") ? "careers" : "/careers");
            }

            // Fetch careers page
            String careersHtml = httpClient.safeFetch(careersUrl);
            Document careersDoc = Jsoup.parse(careersHtml, careersUrl);
            String text = careersDoc.body().text().toLowerCase();

            // JS-only check: if text is extremely short and has script tags, but no meaningful content
            if (text.length() < 100 && !careersDoc.getElementsByTag("script").isEmpty()) {
                saveCache(domain, jobTitle, CacheStatus.UNKNOWN);
                return buildUnknown("Careers page appears to require JavaScript to render.");
            }

            // Fuzzy match job title
            if (containsFuzzyMatch(text, jobTitle)) {
                saveCache(domain, jobTitle, CacheStatus.MATCHED);
                return buildResult(SignalType.POSITIVE, -10, "Job title was found on the official careers page.", "{\"match\": \"" + ctx.getRequest().getJobTitle() + "\"}", SignalConfidence.HIGH, careersUrl);
            } else {
                saveCache(domain, jobTitle, CacheStatus.NOT_FOUND);
                return buildResult(SignalType.WARNING, 10, "Job title was not found on the official careers page.", "{\"checked\": \"" + careersUrl + "\"}", SignalConfidence.MEDIUM, careersUrl);
            }

        } catch (Exception e) {
            log.warn("CareersPageAnalyzer failed for {}: {}", baseUrl, e.getMessage());
            return buildUnknown("Failed to verify careers page due to network or timeout issues.");
        }
    }

    private void saveCache(String domain, String jobTitle, CacheStatus status) {
        cacheRepository.save(CareersCache.builder()
                .domain(domain)
                .jobTitle(jobTitle)
                .status(status)
                .fetchedAt(LocalDateTime.now())
                .build());
    }

    private boolean isBlockedByRobots(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            String robotsUrl = uri.getScheme() + "://" + uri.getHost() + "/robots.txt";
            String robotsTxt = httpClient.safeFetch(robotsUrl);
            return robotsTxt.toLowerCase().contains("disallow: /");
        } catch (Exception e) {
            // If robots.txt fetch fails, assume allowed
            return false;
        }
    }

    private String findCareersUrl(Document doc, String baseUrl) {
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String text = link.text().toLowerCase();
            if (text.contains("career") || text.contains("jobs") || text.contains("vacancies") || text.contains("we are hiring")) {
                return link.absUrl("href");
            }
        }
        return null;
    }

    private boolean checkPath(String baseUrl, String path) {
        try {
            String url = baseUrl + (baseUrl.endsWith("/") ? path.substring(1) : path);
            httpClient.safeFetch(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsFuzzyMatch(String text, String target) {
        // Strip common punctuation for fuzzy matching
        String cleanText = text.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ");
        String cleanTarget = target.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ");
        
        // Simple subset matching (if target words appear in text)
        String[] targetWords = cleanTarget.split(" ");
        for (String word : targetWords) {
            if (word.length() > 3 && !cleanText.contains(word)) {
                // E.g. "Senior Frontend Developer" -> if "Frontend" is missing, might not be a match
                // However, titles can differ slightly (e.g. "Front-End Engineer").
                // For this MVP fuzzy match, we'll just check if the exact string or a close variant exists.
                return cleanText.contains(cleanTarget);
            }
        }
        return true; 
    }

    private SignalResult buildResult(SignalType type, int points, String explanation, String evidenceJson, SignalConfidence confidence, String source) {
        return SignalResult.builder()
                .signalName("Careers Page Match")
                .signalType(type)
                .points(points)
                .explanation(explanation)
                .evidenceJson(evidenceJson)
                .confidence(confidence)
                .dataSource(source)
                .build();
    }

    private SignalResult buildUnknown(String explanation) {
        return SignalResult.builder()
                .signalName("Careers Page Match")
                .signalType(SignalType.UNKNOWN)
                .points(0)
                .explanation(explanation)
                .confidence(SignalConfidence.HIGH)
                .dataSource("Website")
                .build();
    }
}
