package com.finance.personalfinancemanager.service;

import com.finance.personalfinancemanager.dto.auth.AuthResponse;
import com.finance.personalfinancemanager.dto.auth.LoginRequest;
import com.finance.personalfinancemanager.dto.auth.RegisterRequest;
import com.finance.personalfinancemanager.entity.Category;
import com.finance.personalfinancemanager.entity.User;
import com.finance.personalfinancemanager.entity.CategoryType;
import com.finance.personalfinancemanager.exception.ConflictException;
import com.finance.personalfinancemanager.exception.UnauthorizedException;
import com.finance.personalfinancemanager.repository.CategoryRepository;
import com.finance.personalfinancemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");
        user.setPassword("$2a$10$encodedpassword");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("User registered successfully", response.getMessage());
        
        verify(userRepository).existsByUsername("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(categoryRepository, times(7)).save(any(Category.class)); // 7 default categories
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        Long userId = authService.authenticate(loginRequest);

        // Assert
        assertEquals(1L, userId);
        verify(userRepository).findByUsername("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedpassword");
    }

    @Test
    void authenticate_UserNotFound_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> authService.authenticate(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByUsername("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedpassword");
    }

    @Test
    void register_CreatesDefaultCategories() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        authService.register(registerRequest);

        // Assert
        verify(categoryRepository, times(7)).save(any(Category.class));
        // Verify each category type is created
        verify(categoryRepository).save(argThat(category -> 
            category.getName().equals("Salary") && category.getType() == CategoryType.INCOME
        ));
        verify(categoryRepository).save(argThat(category -> 
            category.getName().equals("Food") && category.getType() == CategoryType.EXPENSE
        ));
    }
}
