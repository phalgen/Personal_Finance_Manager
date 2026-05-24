package com.finance.personalfinancemanager.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private Long userId;

    public static AuthResponse success(Long userId) {
        return new AuthResponse("User registered successfully", userId);
    }
}
