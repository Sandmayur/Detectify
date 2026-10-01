package com.fakecompanydetector.service.analysis.analyzers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(6) // Runs after CareersPageAnalyzer
public class WebPresenceAnalyzer implements RiskSignalAnalyzer {

    private final SafeHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${fcd.analyzers.web-presence.max-warning-points:20}")
    private int maxWarningPoints;
    @Value("${fcd.analyzers.web-presence.max-positive-points:-15}")
    private int maxPositivePoints;
    @Value("${fcd.analyzers.web-presence.min-word-count:50}")
    private int minWordCount;

    @Value("${fcd.analyzers.web-presence.points.about-us-missing:3}")
    private int aboutUsMissing;
    @Value("${fcd.analyzers.web-presence.points.about-us-present:-3}")
    private int aboutUsPresent;
    @Value("${fcd.analyzers.web-presence.points.contact-missing:5}")
    private int contactMissing;
    @Value("${fcd.analyzers.web-presence.points.contact-present:-3}")
    private int contactPresent;
    @Value("${fcd.analyzers.web-presence.points.address-missing:5}")
    private int addressMissing;
    @Value("${fcd.analyzers.web-presence.points.address-present:-3}")
    private int addressPresent;
    @Value("${fcd.analyzers.web-presence.points.https-missing:10}")
    private int httpsMissing;
    @Value("${fcd.analyzers.web-presence.points.social-missing:5}")
    private int socialMissing;
    @Value("${fcd.analyzers.web-presence.points.social-present:-3}")
    private int socialPresent;
    @Value("${fcd.analyzers.web-presence.points.thin-content:10}")
    private int thinContent;

    // Pattern for simple address detection: 5 or 6 digit number (zip/pin code) near state/city keywords
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(?i)(\\b\\d{5,6}\\b).{0,50}?(state|city|avenue|street|st\\.|ave|road|rd|blvd|lane|ln|suite|ste|floor|fl|building|bldg|pin|zip)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?i)(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}");

    @Override
    public boolean supports(AnalysisContext ctx) {
        return ctx.getResolvedDomain() != null && !ctx.getResolvedDomain().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        String baseUrl = ctx.getRequest().getCompanyIdentifier();
        if (baseUrl == null || !baseUrl.startsWith("http")) {
            baseUrl = "https://" + ctx.getResolvedDomain();
        }

        try {
            // Check robots.txt
            if (ctx.getIsRobotsTxtBlocked() == null) {
                ctx.setIsRobotsTxtBlocked(isBlockedByRobots(baseUrl));
            }
            if (Boolean.TRUE.equals(ctx.getIsRobotsTxtBlocked())) {
                return buildUnknown("Web presence check blocked by robots.txt.");
            }

            // Fetch homepage
            if (ctx.getHomepageHtml() == null) {
                String fetchedHtml = httpClient.safeFetch(baseUrl);
                ctx.setHomepageHtml(fetchedHtml);
                ctx.setHomepageDoc(Jsoup.parse(fetchedHtml, baseUrl));
            }
            
            Document homeDoc = ctx.getHomepageDoc();
            String homeText = homeDoc.body() != null ? homeDoc.body().text() : "";
            Elements links = homeDoc.select("a[href]");

            int totalPoints = 0;
            List<String> explanations = new ArrayList<>();
            ObjectNode evidence = objectMapper.createObjectNode();

            // 1. About Us
            boolean hasAboutUs = false;
            for (Element link : links) {
                String text = link.text().toLowerCase();
                String href = link.attr("href").toLowerCase();
                if (text.contains("about") || text.contains("company") || href.contains("about")) {
                    hasAboutUs = true;
                    evidence.put("aboutUsUrl", link.absUrl("href"));
                    break;
                }
            }
            if (hasAboutUs) {
                totalPoints += aboutUsPresent;
                explanations.add("Found 'About Us' or 'Company' page link.");
            } else {
                totalPoints += aboutUsMissing;
                explanations.add("No 'About Us' or 'Company' page link found on homepage.");
            }

            // 2. Contact Us / Email / Phone
            boolean hasContactLink = false;
            for (Element link : links) {
                String text = link.text().toLowerCase();
                String href = link.attr("href").toLowerCase();
                if (text.contains("contact") || href.contains("contact")) {
                    hasContactLink = true;
                    evidence.put("contactUrl", link.absUrl("href"));
                    break;
                }
            }
            boolean hasEmail = EMAIL_PATTERN.matcher(homeText).find();
            boolean hasPhone = PHONE_PATTERN.matcher(homeText).find();
            
            if (hasContactLink || hasEmail || hasPhone) {
                totalPoints += contactPresent;
                explanations.add("Found Contact Us page or visible contact details.");
                evidence.put("hasContactLink", hasContactLink);
                evidence.put("hasEmail", hasEmail);
                evidence.put("hasPhone", hasPhone);
            } else {
                totalPoints += contactMissing;
                explanations.add("No Contact Us page or visible contact details were found on the homepage.");
            }

            // 3. Physical Address
            boolean hasAddress = ADDRESS_PATTERN.matcher(homeText).find();
            if (hasAddress) {
                totalPoints += addressPresent;
                explanations.add("Physical address pattern detected on homepage.");
                evidence.put("hasAddress", true);
            } else {
                totalPoints += addressMissing;
                explanations.add("No physical address detected on homepage.");
            }

            // 4. HTTPS
            if (!baseUrl.startsWith("https://")) {
                totalPoints += httpsMissing;
                explanations.add("Site is not served over HTTPS.");
                evidence.put("isHttps", false);
            } else {
                evidence.put("isHttps", true);
            }

            // 5. Social Media Links
            boolean hasSocial = false;
            for (Element link : links) {
                String href = link.attr("href").toLowerCase();
                if ((href.contains("linkedin.com") || href.contains("twitter.com") || href.contains("x.com") || 
                     href.contains("instagram.com") || href.contains("facebook.com")) && !href.equals("#")) {
                    hasSocial = true;
                    evidence.put("socialUrl", link.absUrl("href"));
                    break;
                }
            }
            if (hasSocial) {
                totalPoints += socialPresent;
                explanations.add("Found at least one valid social media link.");
            } else {
                totalPoints += socialMissing;
                explanations.add("No valid social media links found.");
            }

            // 6. Thin Content
            String[] words = homeText.split("\\s+");
            if (words.length < minWordCount) {
                totalPoints += thinContent;
                explanations.add("Homepage has very little content (thin content placeholder).");
                evidence.put("wordCount", words.length);
            }

            // Cap points
            if (totalPoints > maxWarningPoints) {
                totalPoints = maxWarningPoints;
            } else if (totalPoints < maxPositivePoints) {
                totalPoints = maxPositivePoints;
            }

            SignalType type = SignalType.NEUTRAL;
            if (totalPoints > 0) type = SignalType.WARNING;
            else if (totalPoints < 0) type = SignalType.POSITIVE;

            return SignalResult.builder()
                    .signalName("Web Presence")
                    .signalType(type)
                    .points(totalPoints)
                    .explanation(String.join(" ", explanations))
                    .evidenceJson(objectMapper.writeValueAsString(evidence))
                    .confidence(SignalConfidence.HIGH)
                    .dataSource("Website")
                    .build();

        } catch (Exception e) {
            log.warn("WebPresenceAnalyzer failed for {}: {}", baseUrl, e.getMessage());
            return buildUnknown("Failed to analyze web presence due to network or timeout issues.");
        }
    }

    private boolean isBlockedByRobots(String baseUrl) {
        try {
            URI uri = new URI(baseUrl);
            String robotsUrl = uri.getScheme() + "://" + uri.getHost() + "/robots.txt";
            String robotsTxt = httpClient.safeFetch(robotsUrl);
            return robotsTxt.toLowerCase().contains("disallow: /");
        } catch (Exception e) {
            return false;
        }
    }

    private SignalResult buildUnknown(String explanation) {
        return SignalResult.builder()
                .signalName("Web Presence")
                .signalType(SignalType.UNKNOWN)
                .points(0)
                .explanation(explanation)
                .confidence(SignalConfidence.HIGH)
                .dataSource("Website")
                .build();
    }
}
