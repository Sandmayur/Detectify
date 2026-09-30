package com.fakecompanydetector.controller;

import com.fakecompanydetector.dto.AnalysisRequest;
import com.fakecompanydetector.dto.AnalysisResponse;
import com.fakecompanydetector.service.analysis.AnalysisOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisOrchestrator analysisOrchestrator;

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisOrchestrator.analyze(request);
        return ResponseEntity.ok(response);
    }
}
