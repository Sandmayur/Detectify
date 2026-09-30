package com.fakecompanydetector.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeHttpClientTest {

    private final SafeHttpClient safeHttpClient = new SafeHttpClient();

    @Test
    void testLocalhostBlocked() {
        IOException exception = assertThrows(IOException.class, () -> {
            safeHttpClient.safeFetch("http://localhost:8080");
        });
        assertTrue(exception.getMessage().contains("SSRF attempt blocked"));
    }

    @Test
    void testLoopbackBlocked() {
        IOException exception = assertThrows(IOException.class, () -> {
            safeHttpClient.safeFetch("http://127.0.0.1/admin");
        });
        assertTrue(exception.getMessage().contains("SSRF attempt blocked"));
    }

    @Test
    void testPrivateNetworkBlocked() {
        IOException exception = assertThrows(IOException.class, () -> {
            safeHttpClient.safeFetch("http://192.168.1.1");
        });
        assertTrue(exception.getMessage().contains("SSRF attempt blocked"));
    }

    @Test
    void testCloudMetadataBlocked() {
        // AWS Metadata IP is 169.254.169.254 (Link Local)
        IOException exception = assertThrows(IOException.class, () -> {
            safeHttpClient.safeFetch("http://169.254.169.254/latest/meta-data/");
        });
        assertTrue(exception.getMessage().contains("SSRF attempt blocked"));
    }
}
