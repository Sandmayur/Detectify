package com.fakecompanydetector.service.analysis.provider;

import java.time.LocalDateTime;

public interface RegistrationDataProvider {
    /**
     * Attempts to fetch registration data for a company.
     * @param companyName The normalized company name.
     * @return Result of the query.
     */
    RegistrationResult query(String companyName);

    /**
     * Whether this provider is configured and available.
     */
    boolean isConfigured();

    record RegistrationResult(
            boolean found,
            String registrationId,
            String status, // e.g. "ACTIVE", "STRUCK_OFF"
            String source,
            LocalDateTime verifiedAt
    ) {}
}
