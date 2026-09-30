package com.fakecompanydetector.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class SafeHttpClient {

    private final HttpClient httpClient;

    public SafeHttpClient() {
        // Do NOT automatically follow redirects here, we need to manually inspect the IP of the redirect
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Safely fetches content from a URL with SSRF protection, robots.txt awareness is handled externally.
     */
    public String safeFetch(String urlString) throws IOException, InterruptedException {
        return safeFetchWithRedirects(urlString, 0);
    }

    private String safeFetchWithRedirects(String urlString, int redirectCount) throws IOException, InterruptedException {
        if (redirectCount > 3) {
            throw new IOException("Too many redirects");
        }

        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + urlString);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IOException("Invalid protocol. Only HTTP/HTTPS allowed.");
        }

        // Validate IP to prevent SSRF
        InetAddress address = InetAddress.getByName(uri.getHost());
        if (isPrivateOrLocal(address)) {
            throw new IOException("SSRF attempt blocked. Private/Local IP resolved for: " + uri.getHost());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", "FakeCompanyDetector-Bot/1.0 (+http://fakecompanydetector.com)")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        int status = response.statusCode();
        if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
            String location = response.headers().firstValue("location").orElse(null);
            if (location == null) {
                throw new IOException("Redirect without Location header");
            }
            URI redirectUri = uri.resolve(location);
            return safeFetchWithRedirects(redirectUri.toString(), redirectCount + 1);
        }

        if (status >= 400) {
            throw new IOException("HTTP Error " + status + " fetching " + urlString);
        }

        String body = response.body();
        if (body.length() > 5 * 1024 * 1024) { // Cap at 5MB
            throw new IOException("Response too large");
        }
        return body;
    }

    private boolean isPrivateOrLocal(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()) {
            return true;
        }
        if (address.isSiteLocalAddress()) {
            return true;
        }
        
        byte[] ip = address.getAddress();
        if (ip.length == 4) { // IPv4
            int byte1 = ip[0] & 0xFF;
            int byte2 = ip[1] & 0xFF;
            // 10.0.0.0/8
            if (byte1 == 10) return true;
            // 172.16.0.0/12
            if (byte1 == 172 && byte2 >= 16 && byte2 <= 31) return true;
            // 192.168.0.0/16
            if (byte1 == 192 && byte2 == 168) return true;
            // 100.64.0.0/10 (Carrier-grade NAT)
            if (byte1 == 100 && byte2 >= 64 && byte2 <= 127) return true;
        }
        return false;
    }
}
