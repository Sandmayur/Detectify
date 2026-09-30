package com.fakecompanydetector.service.analysis.analyzers;

import com.fakecompanydetector.entity.enums.SignalType;
import com.fakecompanydetector.service.analysis.AnalysisContext;
import com.fakecompanydetector.service.analysis.RiskSignalAnalyzer;
import com.fakecompanydetector.service.analysis.SignalResult;
import com.fakecompanydetector.service.analysis.provider.RegistrationDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(4)
public class RegistrationAnalyzer implements RiskSignalAnalyzer {

    private final ObjectProvider<RegistrationDataProvider> providers;

    @Override
    public boolean supports(AnalysisContext ctx) {
        // Can only search if we have a company name
        return ctx.getRequest().getCompanyIdentifier() != null && !ctx.getRequest().getCompanyIdentifier().isBlank();
    }

    @Override
    public SignalResult analyze(AnalysisContext ctx) {
        List<RegistrationDataProvider> configuredProviders = providers.stream()
                .filter(RegistrationDataProvider::isConfigured)
                .collect(Collectors.toList());

        if (configuredProviders.isEmpty()) {
            return SignalResult.builder()
                    .signalName("Company Registration")
                    .signalType(SignalType.UNKNOWN)
                    .points(0)
                    .explanation("No registration data source is configured.")
                    .confidence(com.fakecompanydetector.entity.enums.SignalConfidence.HIGH)
                    .dataSource("System")
                    .build();
        }

        // Try the first configured provider
        RegistrationDataProvider provider = configuredProviders.get(0);
        
        try {
            RegistrationDataProvider.RegistrationResult result = provider.query(ctx.getRequest().getCompanyIdentifier());
            
            if (result.found()) {
                boolean isActive = "ACTIVE".equalsIgnoreCase(result.status());
                return SignalResult.builder()
                        .signalName("Company Registration")
                        .signalType(isActive ? SignalType.POSITIVE : SignalType.WARNING)
                        .points(isActive ? -15 : 0)
                        .explanation(isActive 
                                ? "Company is officially registered and active." 
                                : "Company is registered but its status is: " + result.status() + ".")
                        .evidenceJson("{\"id\": \"" + result.registrationId() + "\", \"status\": \"" + result.status() + "\"}")
                        .confidence(com.fakecompanydetector.entity.enums.SignalConfidence.HIGH)
                        .dataSource(result.source())
                        .build();
            } else {
                return SignalResult.builder()
                        .signalName("Company Registration")
                        .signalType(SignalType.RED_FLAG)
                        .points(15)
                        .explanation("No matching registration record found in official sources.")
                        .confidence(com.fakecompanydetector.entity.enums.SignalConfidence.MEDIUM)
                        .dataSource(provider.getClass().getSimpleName())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Registration provider failed for {}", ctx.getRequest().getCompanyIdentifier(), e);
            return SignalResult.builder()
                    .signalName("Company Registration")
                    .signalType(SignalType.UNKNOWN)
                    .points(0)
                    .explanation("Failed to query registration data source.")
                    .confidence(com.fakecompanydetector.entity.enums.SignalConfidence.LOW)
                    .dataSource(provider.getClass().getSimpleName())
                    .build();
        }
    }
}
