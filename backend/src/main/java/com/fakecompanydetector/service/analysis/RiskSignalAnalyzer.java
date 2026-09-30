package com.fakecompanydetector.service.analysis;

public interface RiskSignalAnalyzer {
    boolean supports(AnalysisContext ctx);
    SignalResult analyze(AnalysisContext ctx);
}
