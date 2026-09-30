package com.fakecompanydetector.controller;

import com.fakecompanydetector.dto.DisputeResponse;
import com.fakecompanydetector.dto.SubmitDisputeRequest;
import com.fakecompanydetector.service.dispute.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<DisputeResponse> submitDispute(@Valid @RequestBody SubmitDisputeRequest request) {
        DisputeResponse response = disputeService.submitDispute(request);
        return ResponseEntity.ok(response);
    }
}
