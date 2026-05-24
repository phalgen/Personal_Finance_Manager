package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.auth.AuthResponse;
import com.finance.personalfinancemanager.dto.auth.LoginRequest;
import com.finance.personalfinancemanager.dto.auth.RegisterRequest;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.exception.ConflictException;
import com.finance.personalfinancemanager.exception.UnauthorizedException;
import com.finance.personalfinancemanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;

    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      CategoryService categoryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryService = categoryService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        user = userRepository.save(user);

        // Initialize default categories for the new user
        categoryService.initializeDefaultCategories(user);

        return AuthResponse.success(user.getId());
    }

    public Long authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user.getId();
    }
}
