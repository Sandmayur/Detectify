package com.fakecompanydetector.controller;

import com.fakecompanydetector.service.authentication.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UUID userId,
            @RequestBody Map<String, String> request) {
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UUID userId) {
        userService.deleteAccount(userId);
        return ResponseEntity.ok().build();
    }
}
