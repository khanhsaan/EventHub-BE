package com.csci334.EventHub.service;

import com.csci334.EventHub.entity.User;
import com.csci334.EventHub.entity.enums.Role;
import com.csci334.EventHub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // Setup test user 1 - ATTENDEE
        testUser1 = new User();
        testUser1.setId("123456");
        testUser1.setEmail("john.doe@example.com");
        testUser1.setPassword("encodedPassword1");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setRole(Role.ATTENDEE);
        testUser1.setCreatedAt(LocalDateTime.now());

        // Setup test user 2 - ORGANIZER
        testUser2 = new User();
        testUser2.setId("789012");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setPassword("encodedPassword2");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setRole(Role.ORGANIZER);
        testUser2.setCreatedAt(LocalDateTime.now());

        // Setup test user 3 - ATTENDEE
        testUser3 = new User();
        testUser3.setId("345678");
        testUser3.setEmail("bob.johnson@example.com");
        testUser3.setPassword("encodedPassword3");
        testUser3.setFirstName("Bob");
        testUser3.setLastName("Johnson");
        testUser3.setRole(Role.ATTENDEE);
        testUser3.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Read Operations Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("Should return all users when getAll is called")
        void getAll_ShouldReturnAllUsers() {
            // Arrange
            System.out.println("\n=== TEST: Get All Users ===");
            List<User> allUsers = Arrays.asList(testUser1, testUser2, testUser3);
            when(userRepository.findAll()).thenReturn(allUsers);

            System.out.println("Expected Output: List of " + allUsers.size() + " users");

            // Act
            List<User> result = userService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " users");
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(allUsers, result);

            verify(userRepository).findAll();
            System.out.println("✅ TEST PASSED: Retrieved all users successfully!");
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAll_ShouldReturnEmptyList_WhenNoUsersExist() {
            // Arrange
            System.out.println("\n=== TEST: Get All Users - Empty Database ===");
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            System.out.println("Expected Output: Empty list");

            // Act
            List<User> result = userService.getAll();

            // Assert
            System.out.println("Actual Output: List of " + result.size() + " users");
            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(userRepository).findAll();
            System.out.println("✅ TEST PASSED: Empty list returned correctly!");
        }

        @Test
        @DisplayName("Should return user when getById is called with existing ID")
        void getById_ShouldReturnUser_WhenUserExists() {
            // Arrange
            System.out.println("\n=== TEST: Get User By ID ===");
            System.out.println("Input ID: " + testUser1.getId());
            when(userRepository.findById(testUser1.getId())).thenReturn(Optional.of(testUser1));

            System.out.println("Expected Output: User found");

            // Act
            Optional<User> result = userService.getById(testUser1.getId());

            // Assert
            System.out.println("Actual Output: User " + (result.isPresent() ? "found" : "not found"));
            assertTrue(result.isPresent());
            assertEquals(testUser1, result.get());

            verify(userRepository).findById(testUser1.getId());
            System.out.println("✅ TEST PASSED: User retrieved by ID successfully!");
        }

        @Test
        @DisplayName("Should return empty Optional when getById is called with non-existing ID")
        void getById_ShouldReturnEmpty_WhenUserDoesNotExist() {
            // Arrange
            System.out.println("\n=== TEST: Get User By Non-Existing ID ===");
            String nonExistingId = "999999";
            System.out.println("Input ID: " + nonExistingId + " (does not exist)");
            when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: Empty Optional");

            // Act
            Optional<User> result = userService.getById(nonExistingId);

            // Assert
            System.out.println("Actual Output: " + (result.isPresent() ? "User found" : "Empty Optional"));
            assertFalse(result.isPresent());

            verify(userRepository).findById(nonExistingId);
            System.out.println("✅ TEST PASSED: Empty Optional returned for non-existing user!");
        }

        @Test
        @DisplayName("Should return user when getByEmail is called with existing email")
        void getByEmail_ShouldReturnUser_WhenEmailExists() {
            // Arrange
            System.out.println("\n=== TEST: Get User By Email ===");
            System.out.println("Input Email: " + testUser1.getEmail());
            when(userRepository.findByEmail(testUser1.getEmail())).thenReturn(Optional.of(testUser1));

            System.out.println("Expected Output: User found");

            // Act
            Optional<User> result = userService.getByEmail(testUser1.getEmail());

            // Assert
            System.out.println("Actual Output: User " + (result.isPresent() ? "found" : "not found"));
            assertTrue(result.isPresent());
            assertEquals(testUser1, result.get());

            verify(userRepository).findByEmail(testUser1.getEmail());
            System.out.println("✅ TEST PASSED: User retrieved by email successfully!");
        }

        @Test
        @DisplayName("Should return true when emailExists is called with existing email")
        void emailExists_ShouldReturnTrue_WhenEmailExists() {
            // Arrange
            System.out.println("\n=== TEST: Check Email Exists ===");
            System.out.println("Input Email: " + testUser1.getEmail());
            when(userRepository.existsByEmail(testUser1.getEmail())).thenReturn(true);

            System.out.println("Expected Output: true");

            // Act
            boolean result = userService.emailExists(testUser1.getEmail());

            // Assert
            System.out.println("Actual Output: " + result);
            assertTrue(result);

            verify(userRepository).existsByEmail(testUser1.getEmail());
            System.out.println("✅ TEST PASSED: Email existence check successful!");
        }

        @Test
        @DisplayName("Should return false when emailExists is called with non-existing email")
        void emailExists_ShouldReturnFalse_WhenEmailDoesNotExist() {
            // Arrange
            System.out.println("\n=== TEST: Check Non-Existing Email ===");
            String nonExistingEmail = "nonexistent@example.com";
            System.out.println("Input Email: " + nonExistingEmail);
            when(userRepository.existsByEmail(nonExistingEmail)).thenReturn(false);

            System.out.println("Expected Output: false");

            // Act
            boolean result = userService.emailExists(nonExistingEmail);

            // Assert
            System.out.println("Actual Output: " + result);
            assertFalse(result);

            verify(userRepository).existsByEmail(nonExistingEmail);
            System.out.println("✅ TEST PASSED: Non-existing email check successful!");
        }

        @Test
        @DisplayName("Should return users by role when getByRole is called")
        void getByRole_ShouldReturnUsersWithRole() {
            // Arrange
            System.out.println("\n=== TEST: Get Users By Role ===");
            List<User> attendees = Arrays.asList(testUser1, testUser3);
            System.out.println("Input Role: " + Role.ATTENDEE);
            when(userRepository.findByRole(Role.ATTENDEE)).thenReturn(attendees);

            System.out.println("Expected Output: " + attendees.size() + " attendees");

            // Act
            List<User> result = userService.getByRole(Role.ATTENDEE);

            // Assert
            System.out.println("Actual Output: " + result.size() + " users found");
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(attendees, result);

            verify(userRepository).findByRole(Role.ATTENDEE);
            System.out.println("✅ TEST PASSED: Users retrieved by role successfully!");
        }
    }

    @Nested
    @DisplayName("Create and Update Operations Tests")
    class CreateAndUpdateTests {

        @Test
        @DisplayName("Should create user successfully")
        void create_ShouldCreateUser() {
            // Arrange
            System.out.println("\n=== TEST: Create User ===");
            User newUser = new User();
            newUser.setEmail("new@example.com");
            newUser.setFirstName("New");
            newUser.setLastName("User");

            System.out.println("Input User: " + newUser.getEmail());
            when(userRepository.save(newUser)).thenReturn(testUser1);

            System.out.println("Expected Output: User created with generated ID");

            // Act
            User result = userService.create(newUser);

            // Assert
            System.out.println("Actual Output: User created with ID " + result.getId());
            assertNotNull(result);
            assertEquals(testUser1, result);

            verify(userRepository).save(newUser);
            System.out.println("✅ TEST PASSED: User created successfully!");
        }

        @Test
        @DisplayName("Should update user successfully")
        void update_ShouldUpdateUser() {
            // Arrange
            System.out.println("\n=== TEST: Update User ===");
            String userId = "123456";
            User updatedUser = new User();
            updatedUser.setEmail("updated@example.com");
            updatedUser.setFirstName("Updated");
            updatedUser.setLastName("User");

            System.out.println("Input User ID: " + userId);
            System.out.println("Updated Email: " + updatedUser.getEmail());

            when(userRepository.save(updatedUser)).thenReturn(updatedUser);

            System.out.println("Expected Output: User updated with same ID");

            // Act
            User result = userService.update(userId, updatedUser);

            // Assert
            System.out.println("Actual Output: User updated");
            assertNotNull(result);
            assertEquals(userId, updatedUser.getId()); // ID should be set
            assertEquals(updatedUser, result);

            verify(userRepository).save(updatedUser);
            System.out.println("✅ TEST PASSED: User updated successfully!");
        }

        @Test
        @DisplayName("Should update profile successfully when user exists")
        void updateProfile_ShouldUpdateProfile_WhenUserExists() {
            // Arrange
            System.out.println("\n=== TEST: Update User Profile ===");
            String userId = testUser1.getId();
            String newFirstName = "UpdatedJohn";
            String newLastName = "UpdatedDoe";

            System.out.println("Input User ID: " + userId);
            System.out.println("New FirstName: " + newFirstName);
            System.out.println("New LastName: " + newLastName);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(userRepository.save(testUser1)).thenReturn(testUser1);

            System.out.println("Expected Output: Profile updated successfully");

            // Act
            User result = userService.updateProfile(userId, newFirstName, newLastName);

            // Assert
            System.out.println("Actual Output: Profile updated");
            System.out.println("New FirstName: " + testUser1.getFirstName());
            System.out.println("New LastName: " + testUser1.getLastName());

            assertNotNull(result);
            assertEquals(newFirstName, testUser1.getFirstName());
            assertEquals(newLastName, testUser1.getLastName());

            verify(userRepository).findById(userId);
            verify(userRepository).save(testUser1);
            System.out.println("✅ TEST PASSED: Profile updated successfully!");
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when updating profile of non-existing user")
        void updateProfile_ShouldThrowException_WhenUserDoesNotExist() {
            // Arrange
            System.out.println("\n=== TEST: Update Profile - User Not Found ===");
            String nonExistingId = "999999";
            System.out.println("Input User ID: " + nonExistingId + " (does not exist)");

            when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: NoSuchElementException");

            // Act & Assert
            NoSuchElementException exception = assertThrows(
                    NoSuchElementException.class,
                    () -> userService.updateProfile(nonExistingId, "New", "Name")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertTrue(exception.getMessage().contains("User not found with id: " + nonExistingId));

            verify(userRepository).findById(nonExistingId);
            verify(userRepository, never()).save(any(User.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing user!");
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user successfully")
        void delete_ShouldDeleteUser() {
            // Arrange
            System.out.println("\n=== TEST: Delete User ===");
            String userId = testUser1.getId();
            System.out.println("Input User ID: " + userId);

            doNothing().when(userRepository).deleteById(userId);

            System.out.println("Expected Output: User deleted");

            // Act
            userService.delete(userId);

            // Assert
            System.out.println("Actual Output: Delete operation completed");
            verify(userRepository).deleteById(userId);
            System.out.println("✅ TEST PASSED: User deleted successfully!");
        }

        @Test
        @DisplayName("Should handle delete of non-existing user")
        void delete_ShouldHandleNonExistingUser() {
            // Arrange
            System.out.println("\n=== TEST: Delete Non-Existing User ===");
            String nonExistingId = "999999";
            System.out.println("Input User ID: " + nonExistingId + " (does not exist)");

            doNothing().when(userRepository).deleteById(nonExistingId);

            System.out.println("Expected Output: No exception (repository handles gracefully)");

            // Act
            userService.delete(nonExistingId);

            // Assert
            System.out.println("Actual Output: Delete operation completed without exception");
            verify(userRepository).deleteById(nonExistingId);
            System.out.println("✅ TEST PASSED: Delete handled gracefully for non-existing user!");
        }
    }

    @Nested
    @DisplayName("Password Management Tests")
    class PasswordManagementTests {

        @Test
        @DisplayName("Should update password successfully when current password is correct")
        void updatePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() {
            // Arrange
            System.out.println("\n=== TEST: Update Password - Success ===");
            String userId = testUser1.getId();
            String currentPassword = "oldPassword";
            String newPassword = "newPassword";
            String encodedNewPassword = "encodedNewPassword";

            System.out.println("Input User ID: " + userId);
            System.out.println("Current Password: " + currentPassword);
            System.out.println("New Password: " + newPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(passwordEncoder.matches(currentPassword, testUser1.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
            when(userRepository.save(testUser1)).thenReturn(testUser1);

            System.out.println("Expected Output: Password updated successfully");

            // Act
            userService.updatePassword(userId, currentPassword, newPassword);

            // Assert
            System.out.println("Actual Output: Password updated");
            System.out.println("New Encoded Password: " + testUser1.getPassword());

            assertEquals(encodedNewPassword, testUser1.getPassword());

            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(currentPassword, "encodedPassword1");
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUser1);
            System.out.println("✅ TEST PASSED: Password updated successfully!");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when current password is incorrect")
        void updatePassword_ShouldThrowException_WhenCurrentPasswordIsIncorrect() {
            // Arrange
            System.out.println("\n=== TEST: Update Password - Wrong Current Password ===");
            String userId = testUser1.getId();
            String wrongCurrentPassword = "wrongPassword";
            String newPassword = "newPassword";

            System.out.println("Input User ID: " + userId);
            System.out.println("Current Password: " + wrongCurrentPassword + " (incorrect)");

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(passwordEncoder.matches(wrongCurrentPassword, testUser1.getPassword())).thenReturn(false);

            System.out.println("Expected Output: IllegalStateException");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> userService.updatePassword(userId, wrongCurrentPassword, newPassword)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Current password is incorrect", exception.getMessage());

            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(wrongCurrentPassword, testUser1.getPassword());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            System.out.println("✅ TEST PASSED: Exception thrown for incorrect current password!");
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when updating password of non-existing user")
        void updatePassword_ShouldThrowException_WhenUserDoesNotExist() {
            // Arrange
            System.out.println("\n=== TEST: Update Password - User Not Found ===");
            String nonExistingId = "999999";
            System.out.println("Input User ID: " + nonExistingId + " (does not exist)");

            when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            System.out.println("Expected Output: NoSuchElementException");

            // Act & Assert
            NoSuchElementException exception = assertThrows(
                    NoSuchElementException.class,
                    () -> userService.updatePassword(nonExistingId, "current", "new")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertTrue(exception.getMessage().contains("User not found with id: " + nonExistingId));

            verify(userRepository).findById(nonExistingId);
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            System.out.println("✅ TEST PASSED: Exception thrown for non-existing user!");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void methods_ShouldHandleNullInputs() {
            System.out.println("\n=== TEST: Handle Null Inputs ===");

            // Test getById with null
            when(userRepository.findById(null)).thenReturn(Optional.empty());
            Optional<User> result1 = userService.getById(null);
            assertFalse(result1.isPresent());

            // Test getByEmail with null
            when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
            Optional<User> result2 = userService.getByEmail(null);
            assertFalse(result2.isPresent());

            // Test emailExists with null
            when(userRepository.existsByEmail(null)).thenReturn(false);
            boolean result3 = userService.emailExists(null);
            assertFalse(result3);

            System.out.println("✅ TEST PASSED: Null inputs handled gracefully!");
        }

        @Test
        @DisplayName("Should handle repository save failure")
        void create_ShouldPropagateException_WhenRepositoryFails() {
            // Arrange
            System.out.println("\n=== TEST: Repository Save Failure ===");
            User newUser = new User();
            when(userRepository.save(newUser)).thenThrow(new RuntimeException("Database error"));

            System.out.println("Expected Output: RuntimeException propagated");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.create(newUser)
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Database error", exception.getMessage());
            System.out.println("✅ TEST PASSED: Repository exception propagated correctly!");
        }

        @Test
        @DisplayName("Should handle password encoder failure")
        void updatePassword_ShouldPropagateException_WhenPasswordEncoderFails() {
            // Arrange
            System.out.println("\n=== TEST: Password Encoder Failure ===");
            String userId = testUser1.getId();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser1));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("Encoding error"));

            System.out.println("Expected Output: RuntimeException propagated");

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> userService.updatePassword(userId, "current", "new")
            );

            System.out.println("Actual Output: " + exception.getClass().getSimpleName());
            System.out.println("Exception Message: " + exception.getMessage());

            assertEquals("Encoding error", exception.getMessage());
            System.out.println("✅ TEST PASSED: Password encoder exception propagated correctly!");
        }
    }
}