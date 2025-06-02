package com.csci334.EventHub.service;

import com.csci334.EventHub.dto.LoginRequest;
import com.csci334.EventHub.dto.SignupRequest;
import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignupRequest validSignupRequest;
    private LoginRequest validLoginRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        // Setup valid signup request
        validSignupRequest = new SignupRequest();
        validSignupRequest.setEmail("test@example.com");
        validSignupRequest.setPassword("password123");
        validSignupRequest.setFirstName("John");
        validSignupRequest.setLastName("Doe");
        validSignupRequest.setRole(Role.ATTENDEE);

        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Setup existing user
        existingUser = new User();
        existingUser.setId("123456");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setRole(Role.ATTENDEE);
        existingUser.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Signup Tests")
    class SignupTests {

        @Test
        @DisplayName("Should successfully create new user when email doesn't exist")
        void signup_ShouldCreateUser_WhenEmailDoesNotExist() {
            // Arrange
            System.out.println("\n=== TEST: Successful User Signup ===");
            System.out.println("Input SignupRequest:");
            System.out.println("  Email: " + validSignupRequest.getEmail());
            System.out.println("  Password: " + validSignupRequest.getPassword());
            System.out.println("  FirstName: " + validSignupRequest.getFirstName());
            System.out.println("  LastName: " + validSignupRequest.getLastName());
            System.out.println("  Role: " + validSignupRequest.getRole());

            when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            System.out.println("\nExecuting authService.signup()...");
            User result = authService.signup(validSignupRequest);

            // Assert
            System.out.println("\nExpected Output:");
            System.out.println("  User created successfully");
            System.out.println("  Email should match input: " + validSignupRequest.getEmail());
            System.out.println("  Password should be encoded");

            System.out.println("\nActual Output:");
            System.out.println("  User ID: " + result.getId());
            System.out.println("  Email: " + result.getEmail());
            System.out.println("  FirstName: " + result.getFirstName());
            System.out.println("  LastName: " + result.getLastName());
            System.out.println("  Role: " + result.getRole());

            assertNotNull(result);
            assertEquals(existingUser.getEmail(), result.getEmail());

            // Verify interactions
            verify(userRepository).existsByEmail(validSignupRequest.getEmail());
            verify(passwordEncoder).encode(validSignupRequest.getPassword());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            System.out.println("\nUser object sent to repository:");
            System.out.println("  Email: " + savedUser.getEmail());
            System.out.println("  Encoded Password: " + savedUser.getPassword());
            System.out.println("  FirstName: " + savedUser.getFirstName());
            System.out.println("  LastName: " + savedUser.getLastName());
            System.out.println("  Role: " + savedUser.getRole());

            assertEquals(validSignupRequest.getEmail(), savedUser.getEmail());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertEquals(validSignupRequest.getFirstName(), savedUser.getFirstName());
            assertEquals(validSignupRequest.getLastName(), savedUser.getLastName());
            assertEquals(validSignupRequest.getRole(), savedUser.getRole());

            System.out.println("✅ TEST PASSED: User signup successful!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when email already exists")
        void signup_ShouldThrowException_WhenEmailAlreadyExists() {
            // Arrange
            System.out.println("\n=== TEST: Duplicate Email Signup ===");
            System.out.println("Input SignupRequest:");
            System.out.println("  Email: " + validSignupRequest.getEmail() + " (already exists)");

            when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(true);

            System.out.println("\nExpected Output:");
            System.out.println("  Should throw IllegalStateException");
            System.out.println("  Exception message: 'Email already in use'");
            System.out.println("  Repository save() should NOT be called");

            // Act & Assert
            System.out.println("\nExecuting authService.signup()...");
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> authService.signup(validSignupRequest)
            );

            System.out.println("\nActual Output:");
            System.out.println("  Exception thrown: " + exception.getClass().getSimpleName());
            System.out.println("  Exception message: '" + exception.getMessage() + "'");

            assertEquals("Email already in use", exception.getMessage());

            // Verify that save was never called
            verify(userRepository).existsByEmail(validSignupRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());

            System.out.println("  Repository.save() called: NO ✓");
            System.out.println("  PasswordEncoder.encode() called: NO ✓");
            System.out.println("✅ TEST PASSED: Duplicate email properly rejected!");
        }

        @Test
        @DisplayName("Should handle null email in signup request")
        void signup_ShouldHandleNullEmail() {
            // Arrange
            validSignupRequest.setEmail(null);
            when(userRepository.existsByEmail(null)).thenReturn(false);
            when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            User result = authService.signup(validSignupRequest);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByEmail(null);
        }

        @Test
        @DisplayName("Should handle empty string email in signup request")
        void signup_ShouldHandleEmptyEmail() {
            // Arrange
            validSignupRequest.setEmail("");
            when(userRepository.existsByEmail("")).thenReturn(false);
            when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            User result = authService.signup(validSignupRequest);

            // Assert
            assertNotNull(result);
            verify(userRepository).existsByEmail("");
        }

        @Test
        @DisplayName("Should handle null password in signup request")
        void signup_ShouldHandleNullPassword() {
            // Arrange
            validSignupRequest.setPassword(null);
            when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(null)).thenReturn("encodedNullPassword");
            when(userRepository.save(any(User.class))).thenReturn(existingUser);

            // Act
            User result = authService.signup(validSignupRequest);

            // Assert
            assertNotNull(result);
            verify(passwordEncoder).encode(null);
        }

        @Test
        @DisplayName("Should handle all role types")
        void signup_ShouldHandleAllRoles() {
            // Test each role enum value
            for (Role role : Role.values()) {
                // Arrange
                validSignupRequest.setRole(role);
                validSignupRequest.setEmail("test" + role.name() + "@example.com");

                when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(false);
                when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn("encodedPassword");
                when(userRepository.save(any(User.class))).thenReturn(existingUser);

                // Act
                User result = authService.signup(validSignupRequest);

                // Assert
                assertNotNull(result);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                verify(userRepository).save(userCaptor.capture());
                assertEquals(role, userCaptor.getValue().getRole());

                // Reset mocks for next iteration
                reset(userRepository, passwordEncoder);
            }
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should successfully login with valid credentials")
        void login_ShouldReturnUser_WhenCredentialsAreValid() {
            // Arrange
            System.out.println("\n=== TEST: Successful User Login ===");
            System.out.println("Input LoginRequest:");
            System.out.println("  Email: " + validLoginRequest.getEmail());
            System.out.println("  Password: " + validLoginRequest.getPassword());

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(validLoginRequest.getPassword(), existingUser.getPassword()))
                    .thenReturn(true);

            System.out.println("\nExpected Output:");
            System.out.println("  User found in database");
            System.out.println("  Password matches encoded password");
            System.out.println("  Return user object");

            // Act
            System.out.println("\nExecuting authService.login()...");
            User result = authService.login(validLoginRequest);

            // Assert
            System.out.println("\nActual Output:");
            System.out.println("  User ID: " + result.getId());
            System.out.println("  Email: " + result.getEmail());
            System.out.println("  FirstName: " + result.getFirstName());
            System.out.println("  LastName: " + result.getLastName());
            System.out.println("  Role: " + result.getRole());

            assertNotNull(result);
            assertEquals(existingUser.getEmail(), result.getEmail());
            assertEquals(existingUser.getId(), result.getId());

            verify(userRepository).findByEmail(validLoginRequest.getEmail());
            verify(passwordEncoder).matches(validLoginRequest.getPassword(), existingUser.getPassword());

            System.out.println("  Repository.findByEmail() called: YES ✓");
            System.out.println("  PasswordEncoder.matches() called: YES ✓");
            System.out.println("✅ TEST PASSED: User login successful!");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user does not exist")
        void login_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
            // Arrange
            System.out.println("\n=== TEST: Login with Non-existent User ===");
            System.out.println("Input LoginRequest:");
            System.out.println("  Email: " + validLoginRequest.getEmail() + " (does not exist)");
            System.out.println("  Password: " + validLoginRequest.getPassword());

            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.empty());

            System.out.println("\nExpected Output:");
            System.out.println("  Should throw EntityNotFoundException");
            System.out.println("  Exception message: 'User not found'");
            System.out.println("  Password matching should NOT be attempted");

            // Act & Assert
            System.out.println("\nExecuting authService.login()...");
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> authService.login(validLoginRequest)
            );

            System.out.println("\nActual Output:");
            System.out.println("  Exception thrown: " + exception.getClass().getSimpleName());
            System.out.println("  Exception message: '" + exception.getMessage() + "'");

            assertEquals("User not found", exception.getMessage());

            verify(userRepository).findByEmail(validLoginRequest.getEmail());
            verify(passwordEncoder, never()).matches(anyString(), anyString());

            System.out.println("  Repository.findByEmail() called: YES ✓");
            System.out.println("  PasswordEncoder.matches() called: NO ✓");
            System.out.println("✅ TEST PASSED: Non-existent user properly rejected!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password is incorrect")
        void login_ShouldThrowIllegalArgumentException_WhenPasswordIsIncorrect() {
            // Arrange
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(validLoginRequest.getPassword(), existingUser.getPassword()))
                    .thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("Invalid credentials", exception.getMessage());

            verify(userRepository).findByEmail(validLoginRequest.getEmail());
            verify(passwordEncoder).matches(validLoginRequest.getPassword(), existingUser.getPassword());
        }

        @Test
        @DisplayName("Should handle null email in login request")
        void login_ShouldHandleNullEmail() {
            // Arrange
            validLoginRequest.setEmail(null);
            when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).findByEmail(null);
        }

        @Test
        @DisplayName("Should handle empty email in login request")
        void login_ShouldHandleEmptyEmail() {
            // Arrange
            validLoginRequest.setEmail("");
            when(userRepository.findByEmail("")).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userRepository).findByEmail("");
        }

        @Test
        @DisplayName("Should handle null password in login request")
        void login_ShouldHandleNullPassword() {
            // Arrange
            validLoginRequest.setPassword(null);
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(null, existingUser.getPassword()))
                    .thenReturn(false);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("Invalid credentials", exception.getMessage());
            verify(passwordEncoder).matches(null, existingUser.getPassword());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle repository save failure during signup")
        void signup_ShouldPropagateException_WhenRepositorySaveFails() {
            // Arrange
            when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authService.signup(validSignupRequest)
            );

            assertEquals("Database error", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle password encoder failure during signup")
        void signup_ShouldPropagateException_WhenPasswordEncoderFails() {
            // Arrange
            when(userRepository.existsByEmail(validSignupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(validSignupRequest.getPassword()))
                    .thenThrow(new RuntimeException("Encoding error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authService.signup(validSignupRequest)
            );

            assertEquals("Encoding error", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle repository query failure during login")
        void login_ShouldPropagateException_WhenRepositoryQueryFails() {
            // Arrange
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenThrow(new RuntimeException("Database connection error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("Database connection error", exception.getMessage());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle password encoder failure during login")
        void login_ShouldPropagateException_WhenPasswordEncoderFails() {
            // Arrange
            when(userRepository.findByEmail(validLoginRequest.getEmail()))
                    .thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(validLoginRequest.getPassword(), existingUser.getPassword()))
                    .thenThrow(new RuntimeException("Password matching error"));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authService.login(validLoginRequest)
            );

            assertEquals("Password matching error", exception.getMessage());
        }
    }
}