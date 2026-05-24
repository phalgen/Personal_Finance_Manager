package com.finance.personalfinancemanager.controller;

import com.finance.personalfinancemanager.dto.auth.AuthResponse;
import com.finance.personalfinancemanager.dto.auth.LoginRequest;
import com.finance.personalfinancemanager.dto.auth.RegisterRequest;
import com.finance.personalfinancemanager.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        
        Long userId = authService.authenticate(request);
        
        // Store user info in session
        session.setAttribute("userId", userId);
        session.setAttribute("username", request.getUsername());
        session.setAttribute("authenticated", true);
        
        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
