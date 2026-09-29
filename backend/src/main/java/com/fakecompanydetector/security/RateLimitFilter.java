package com.fakecompanydetector.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, RateLimitData> buckets = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 20;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login")) {
            String clientIp = request.getRemoteAddr();
            RateLimitData bucket = buckets.computeIfAbsent(clientIp, k -> new RateLimitData(Instant.now().getEpochSecond(), new AtomicInteger(0)));
            
            long now = Instant.now().getEpochSecond();
            if (now - bucket.windowStart > 60) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }
            
            if (bucket.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"RATE_LIMIT_EXCEEDED\", \"message\": \"Too many authentication attempts. Please try again later.\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private static class RateLimitData {
        long windowStart;
        AtomicInteger count;

        RateLimitData(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
