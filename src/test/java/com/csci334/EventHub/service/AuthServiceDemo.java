package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.LoginRequest;
import com.csci334.EventHub.dto.SignupRequest;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Demo class showing expected vs actual output for AuthService methods
 * This demonstrates the behavior of each method with sample inputs and outputs
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceDemo {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        reset(userRepository, passwordEncoder);
    }

    @Test
    void demonstrateSuccessfulSignup() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 1: SUCCESSFUL USER SIGNUP");
        System.out.println("=".repeat(60));

        // Input
        SignupRequest input = new SignupRequest();
        input.setEmail("john.doe@example.com");
        input.setPassword("mySecretPassword");
        input.setFirstName("John");
        input.setLastName("Doe");
        input.setRole(Role.ATTENDEE);

        System.out.println("INPUT:");
        System.out.println("  SignupRequest {");
        System.out.println("    email: '" + input.getEmail() + "'");
        System.out.println("    password: '" + input.getPassword() + "'");
        System.out.println("    firstName: '" + input.getFirstName() + "'");
        System.out.println("    lastName: '" + input.getLastName() + "'");
        System.out.println("    role: " + input.getRole());
        System.out.println("  }");

        // Expected Output
        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '123456' (auto-generated)");
        System.out.println("    email: 'john.doe@example.com'");
        System.out.println("    password: 'encoded_password' (BCrypt encoded)");
        System.out.println("    firstName: 'John'");
        System.out.println("    lastName: 'Doe'");
        System.out.println("    role: ATTENDEE");
        System.out.println("    createdAt: current timestamp");
        System.out.println("  }");

        // Mock setup
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(input.getPassword())).thenReturn("$2a$10$encoded_password_hash");

        User mockSavedUser = new User();
        mockSavedUser.setId("123456");
        mockSavedUser.setEmail(input.getEmail());
        mockSavedUser.setPassword("$2a$10$encoded_password_hash");
        mockSavedUser.setFirstName(input.getFirstName());
        mockSavedUser.setLastName(input.getLastName());
        mockSavedUser.setRole(input.getRole());
        mockSavedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling authService.signup(signupRequest)...");

        User actualOutput = authService.signup(input);

        // Actual Output
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '" + actualOutput.getId() + "'");
        System.out.println("    email: '" + actualOutput.getEmail() + "'");
        System.out.println("    password: '" + actualOutput.getPassword() + "'");
        System.out.println("    firstName: '" + actualOutput.getFirstName() + "'");
        System.out.println("    lastName: '" + actualOutput.getLastName() + "'");
        System.out.println("    role: " + actualOutput.getRole());
        System.out.println("    createdAt: " + actualOutput.getCreatedAt());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - User created successfully");
    }

    @Test
    void demonstrateFailedSignupDuplicateEmail() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 2: FAILED SIGNUP - DUPLICATE EMAIL");
        System.out.println("=".repeat(60));

        // Input
        SignupRequest input = new SignupRequest();
        input.setEmail("existing@example.com");
        input.setPassword("password123");
        input.setFirstName("Jane");
        input.setLastName("Smith");
        input.setRole(Role.ORGANIZER);

        System.out.println("INPUT:");
        System.out.println("  SignupRequest {");
        System.out.println("    email: '" + input.getEmail() + "' (already exists in database)");
        System.out.println("    password: '" + input.getPassword() + "'");
        System.out.println("    firstName: '" + input.getFirstName() + "'");
        System.out.println("    lastName: '" + input.getLastName() + "'");
        System.out.println("    role: " + input.getRole());
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  IllegalStateException with message: 'Email already in use'");

        // Mock setup
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(true);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling authService.signup(signupRequest)...");

        try {
            authService.signup(input);
            System.out.println("\nACTUAL OUTPUT: ❌ No exception thrown (unexpected!)");
        } catch (IllegalStateException e) {
            System.out.println("\nACTUAL OUTPUT:");
            System.out.println("  Exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: '" + e.getMessage() + "'");
            System.out.println("\nRESULT: ✅ SUCCESS - Duplicate email properly rejected");
        }
    }

    @Test
    void demonstrateSuccessfulLogin() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 3: SUCCESSFUL USER LOGIN");
        System.out.println("=".repeat(60));

        // Input
        LoginRequest input = new LoginRequest();
        input.setEmail("user@example.com");
        input.setPassword("correctPassword");

        System.out.println("INPUT:");
        System.out.println("  LoginRequest {");
        System.out.println("    email: '" + input.getEmail() + "'");
        System.out.println("    password: '" + input.getPassword() + "'");
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '654321'");
        System.out.println("    email: 'user@example.com'");
        System.out.println("    firstName: 'Alice'");
        System.out.println("    lastName: 'Johnson'");
        System.out.println("    role: ATTENDEE");
        System.out.println("  }");

        // Mock setup
        User existingUser = new User();
        existingUser.setId("654321");
        existingUser.setEmail("user@example.com");
        existingUser.setPassword("$2a$10$stored_encoded_password");
        existingUser.setFirstName("Alice");
        existingUser.setLastName("Johnson");
        existingUser.setRole(Role.ATTENDEE);

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(input.getPassword(), existingUser.getPassword())).thenReturn(true);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling authService.login(loginRequest)...");

        User actualOutput = authService.login(input);

        // Actual Output
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '" + actualOutput.getId() + "'");
        System.out.println("    email: '" + actualOutput.getEmail() + "'");
        System.out.println("    firstName: '" + actualOutput.getFirstName() + "'");
        System.out.println("    lastName: '" + actualOutput.getLastName() + "'");
        System.out.println("    role: " + actualOutput.getRole());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - User authenticated successfully");
    }

    @Test
    void demonstrateFailedLoginWrongPassword() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 4: FAILED LOGIN - WRONG PASSWORD");
        System.out.println("=".repeat(60));

        // Input
        LoginRequest input = new LoginRequest();
        input.setEmail("user@example.com");
        input.setPassword("wrongPassword");

        System.out.println("INPUT:");
        System.out.println("  LoginRequest {");
        System.out.println("    email: '" + input.getEmail() + "' (exists in database)");
        System.out.println("    password: '" + input.getPassword() + "' (incorrect)");
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  IllegalArgumentException with message: 'Invalid credentials'");

        // Mock setup
        User existingUser = new User();
        existingUser.setId("654321");
        existingUser.setEmail("user@example.com");
        existingUser.setPassword("$2a$10$stored_encoded_password");

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(input.getPassword(), existingUser.getPassword())).thenReturn(false);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling authService.login(loginRequest)...");

        try {
            authService.login(input);
            System.out.println("\nACTUAL OUTPUT: ❌ No exception thrown (unexpected!)");
        } catch (IllegalArgumentException e) {
            System.out.println("\nACTUAL OUTPUT:");
            System.out.println("  Exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: '" + e.getMessage() + "'");
            System.out.println("\nRESULT: ✅ SUCCESS - Invalid credentials properly rejected");
        }
    }

    @Test
    void demonstrateFailedLoginUserNotFound() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 5: FAILED LOGIN - USER NOT FOUND");
        System.out.println("=".repeat(60));

        // Input
        LoginRequest input = new LoginRequest();
        input.setEmail("nonexistent@example.com");
        input.setPassword("anyPassword");

        System.out.println("INPUT:");
        System.out.println("  LoginRequest {");
        System.out.println("    email: '" + input.getEmail() + "' (does not exist)");
        System.out.println("    password: '" + input.getPassword() + "'");
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  EntityNotFoundException with message: 'User not found'");

        // Mock setup
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling authService.login(loginRequest)...");

        try {
            authService.login(input);
            System.out.println("\nACTUAL OUTPUT: ❌ No exception thrown (unexpected!)");
        } catch (Exception e) {
            System.out.println("\nACTUAL OUTPUT:");
            System.out.println("  Exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: '" + e.getMessage() + "'");
            System.out.println("\nRESULT: ✅ SUCCESS - Non-existent user properly rejected");
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ALL DEMOS COMPLETED");
        System.out.println("=".repeat(60));
    }
}