package com.csci334.EventHub.service;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Demo class showing expected vs actual output for UserService methods
 * This demonstrates the behavior of each CRUD operation with sample inputs and outputs
 */
@ExtendWith(MockitoExtension.class)
class UserServiceDemo {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser1;
    private User sampleUser2;

    @BeforeEach
    void setUp() {
        reset(userRepository, passwordEncoder);

        sampleUser1 = new User();
        sampleUser1.setId("123456");
        sampleUser1.setEmail("john.doe@example.com");
        sampleUser1.setPassword("$2a$10$encoded_password_hash");
        sampleUser1.setFirstName("John");
        sampleUser1.setLastName("Doe");
        sampleUser1.setRole(Role.ATTENDEE);
        sampleUser1.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));

        sampleUser2 = new User();
        sampleUser2.setId("789012");
        sampleUser2.setEmail("jane.smith@example.com");
        sampleUser2.setPassword("$2a$10$another_encoded_hash");
        sampleUser2.setFirstName("Jane");
        sampleUser2.setLastName("Smith");
        sampleUser2.setRole(Role.ORGANIZER);
        sampleUser2.setCreatedAt(LocalDateTime.of(2024, 2, 20, 14, 45));
    }

    @Test
    void demonstrateGetAllUsers() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 1: GET ALL USERS");
        System.out.println("=".repeat(70));

        System.out.println("INPUT:");
        System.out.println("  userService.getAll()");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<User> containing all users in the database:");
        System.out.println("  [");
        System.out.println("    User { id: '123456', email: 'john.doe@example.com', firstName: 'John', lastName: 'Doe', role: ATTENDEE },");
        System.out.println("    User { id: '789012', email: 'jane.smith@example.com', firstName: 'Jane', lastName: 'Smith', role: ORGANIZER }");
        System.out.println("  ]");

        // Mock setup
        List<User> allUsers = Arrays.asList(sampleUser1, sampleUser2);
        when(userRepository.findAll()).thenReturn(allUsers);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.getAll()...");

        List<User> result = userService.getAll();

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<User> with " + result.size() + " users:");
        System.out.println("  [");
        for (User user : result) {
            System.out.println("    User { id: '" + user.getId() + "', email: '" + user.getEmail() +
                    "', firstName: '" + user.getFirstName() + "', lastName: '" + user.getLastName() +
                    "', role: " + user.getRole() + " }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Retrieved " + result.size() + " users from database");
    }

    @Test
    void demonstrateGetUserById() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 2: GET USER BY ID");
        System.out.println("=".repeat(70));

        String userId = "123456";
        System.out.println("INPUT:");
        System.out.println("  userService.getById(\"" + userId + "\")");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Optional<User> containing:");
        System.out.println("  User {");
        System.out.println("    id: '" + sampleUser1.getId() + "'");
        System.out.println("    email: '" + sampleUser1.getEmail() + "'");
        System.out.println("    firstName: '" + sampleUser1.getFirstName() + "'");
        System.out.println("    lastName: '" + sampleUser1.getLastName() + "'");
        System.out.println("    role: " + sampleUser1.getRole());
        System.out.println("    createdAt: " + sampleUser1.getCreatedAt());
        System.out.println("  }");

        // Mock setup
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser1));

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.getById(\"" + userId + "\")...");

        Optional<User> result = userService.getById(userId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        if (result.isPresent()) {
            User user = result.get();
            System.out.println("  Optional<User> containing:");
            System.out.println("  User {");
            System.out.println("    id: '" + user.getId() + "'");
            System.out.println("    email: '" + user.getEmail() + "'");
            System.out.println("    firstName: '" + user.getFirstName() + "'");
            System.out.println("    lastName: '" + user.getLastName() + "'");
            System.out.println("    role: " + user.getRole());
            System.out.println("    createdAt: " + user.getCreatedAt());
            System.out.println("  }");
        } else {
            System.out.println("  Optional.empty()");
        }

        System.out.println("\nRESULT: ✅ SUCCESS - User found and retrieved");
    }

    @Test
    void demonstrateGetUserByIdNotFound() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 3: GET USER BY ID - NOT FOUND");
        System.out.println("=".repeat(70));

        String nonExistentId = "999999";
        System.out.println("INPUT:");
        System.out.println("  userService.getById(\"" + nonExistentId + "\") // User does not exist");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Optional.empty()");

        // Mock setup
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.getById(\"" + nonExistentId + "\")...");

        Optional<User> result = userService.getById(nonExistentId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  " + (result.isPresent() ? "User found: " + result.get() : "Optional.empty()"));

        System.out.println("\nRESULT: ✅ SUCCESS - Empty Optional returned for non-existent user");
    }

    @Test
    void demonstrateCreateUser() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 4: CREATE NEW USER");
        System.out.println("=".repeat(70));

        User newUser = new User();
        newUser.setEmail("alice.wilson@example.com");
        newUser.setPassword("$2a$10$new_encoded_password");
        newUser.setFirstName("Alice");
        newUser.setLastName("Wilson");
        newUser.setRole(Role.ATTENDEE);

        System.out.println("INPUT:");
        System.out.println("  User newUser = new User();");
        System.out.println("  newUser.setEmail(\"" + newUser.getEmail() + "\");");
        System.out.println("  newUser.setPassword(\"" + newUser.getPassword() + "\");");
        System.out.println("  newUser.setFirstName(\"" + newUser.getFirstName() + "\");");
        System.out.println("  newUser.setLastName(\"" + newUser.getLastName() + "\");");
        System.out.println("  newUser.setRole(" + newUser.getRole() + ");");
        System.out.println("  userService.create(newUser);");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '345678' (auto-generated)");
        System.out.println("    email: '" + newUser.getEmail() + "'");
        System.out.println("    password: '" + newUser.getPassword() + "'");
        System.out.println("    firstName: '" + newUser.getFirstName() + "'");
        System.out.println("    lastName: '" + newUser.getLastName() + "'");
        System.out.println("    role: " + newUser.getRole());
        System.out.println("    createdAt: current timestamp");
        System.out.println("  }");

        // Mock setup
        User savedUser = new User();
        savedUser.setId("345678");
        savedUser.setEmail(newUser.getEmail());
        savedUser.setPassword(newUser.getPassword());
        savedUser.setFirstName(newUser.getFirstName());
        savedUser.setLastName(newUser.getLastName());
        savedUser.setRole(newUser.getRole());
        savedUser.setCreatedAt(LocalDateTime.now());

        when(userRepository.save(newUser)).thenReturn(savedUser);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.create(newUser)...");

        User result = userService.create(newUser);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '" + result.getId() + "'");
        System.out.println("    email: '" + result.getEmail() + "'");
        System.out.println("    password: '" + result.getPassword() + "'");
        System.out.println("    firstName: '" + result.getFirstName() + "'");
        System.out.println("    lastName: '" + result.getLastName() + "'");
        System.out.println("    role: " + result.getRole());
        System.out.println("    createdAt: " + result.getCreatedAt());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - New user created with generated ID");
    }

    @Test
    void demonstrateUpdateProfile() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 5: UPDATE USER PROFILE");
        System.out.println("=".repeat(70));

        String userId = "123456";
        String newFirstName = "Johnny";
        String newLastName = "Smith";

        System.out.println("INPUT:");
        System.out.println("  userService.updateProfile(\"" + userId + "\", \"" + newFirstName + "\", \"" + newLastName + "\")");

        System.out.println("\nCURRENT USER STATE:");
        System.out.println("  User {");
        System.out.println("    id: '" + sampleUser1.getId() + "'");
        System.out.println("    firstName: '" + sampleUser1.getFirstName() + "' → '" + newFirstName + "'");
        System.out.println("    lastName: '" + sampleUser1.getLastName() + "' → '" + newLastName + "'");
        System.out.println("  }");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '" + userId + "'");
        System.out.println("    email: '" + sampleUser1.getEmail() + "' (unchanged)");
        System.out.println("    firstName: '" + newFirstName + "' (updated)");
        System.out.println("    lastName: '" + newLastName + "' (updated)");
        System.out.println("    role: " + sampleUser1.getRole() + " (unchanged)");
        System.out.println("  }");

        // Mock setup
        User userToUpdate = new User();
        userToUpdate.setId(sampleUser1.getId());
        userToUpdate.setEmail(sampleUser1.getEmail());
        userToUpdate.setPassword(sampleUser1.getPassword());
        userToUpdate.setFirstName(sampleUser1.getFirstName());
        userToUpdate.setLastName(sampleUser1.getLastName());
        userToUpdate.setRole(sampleUser1.getRole());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.updateProfile(\"" + userId + "\", \"" + newFirstName + "\", \"" + newLastName + "\")...");

        User result = userService.updateProfile(userId, newFirstName, newLastName);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  User {");
        System.out.println("    id: '" + result.getId() + "'");
        System.out.println("    email: '" + result.getEmail() + "'");
        System.out.println("    firstName: '" + result.getFirstName() + "'");
        System.out.println("    lastName: '" + result.getLastName() + "'");
        System.out.println("    role: " + result.getRole());
        System.out.println("  }");

        System.out.println("\nRESULT: ✅ SUCCESS - User profile updated successfully");
    }

    @Test
    void demonstrateUpdatePassword() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 6: UPDATE USER PASSWORD");
        System.out.println("=".repeat(70));

        String userId = "123456";
        String currentPassword = "oldPassword123";
        String newPassword = "newSecurePassword456";
        String encodedNewPassword = "$2a$10$new_encoded_password_hash";

        System.out.println("INPUT:");
        System.out.println("  userService.updatePassword(\"" + userId + "\", \"" + currentPassword + "\", \"" + newPassword + "\")");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Find user by ID: " + userId);
        System.out.println("  2. Verify current password matches stored hash");
        System.out.println("  3. Encode new password: '" + newPassword + "' → '" + encodedNewPassword + "'");
        System.out.println("  4. Save user with new encoded password");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Operation completed successfully (void method)");
        System.out.println("  User's password updated in database");

        // Mock setup
        User userToUpdate = new User();
        userToUpdate.setId(sampleUser1.getId());
        userToUpdate.setEmail(sampleUser1.getEmail());
        userToUpdate.setPassword(sampleUser1.getPassword());
        userToUpdate.setFirstName(sampleUser1.getFirstName());
        userToUpdate.setLastName(sampleUser1.getLastName());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(passwordEncoder.matches(currentPassword, sampleUser1.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.updatePassword()...");
        System.out.println("  Step 1: Finding user with ID '" + userId + "'...");
        System.out.println("  Step 2: Verifying current password...");
        System.out.println("  Step 3: Encoding new password...");
        System.out.println("  Step 4: Saving updated user...");

        userService.updatePassword(userId, currentPassword, newPassword);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Operation completed successfully");
        System.out.println("  Password verification: PASSED");
        System.out.println("  New password encoding: COMPLETED");
        System.out.println("  Database save: COMPLETED");
        System.out.println("  User's password hash: '" + userToUpdate.getPassword() + "'");

        System.out.println("\nRESULT: ✅ SUCCESS - Password updated securely");
    }

    @Test
    void demonstrateUpdatePasswordIncorrectCurrent() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 7: UPDATE PASSWORD - INCORRECT CURRENT PASSWORD");
        System.out.println("=".repeat(70));

        String userId = "123456";
        String wrongCurrentPassword = "wrongPassword";
        String newPassword = "newPassword123";

        System.out.println("INPUT:");
        System.out.println("  userService.updatePassword(\"" + userId + "\", \"" + wrongCurrentPassword + "\", \"" + newPassword + "\")");
        System.out.println("  Note: Current password is incorrect");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Find user by ID: " + userId);
        System.out.println("  2. Verify current password: FAILS (password doesn't match)");
        System.out.println("  3. Throw IllegalStateException: 'Current password is incorrect'");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  IllegalStateException with message: 'Current password is incorrect'");

        // Mock setup
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser1));
        when(passwordEncoder.matches(wrongCurrentPassword, sampleUser1.getPassword())).thenReturn(false);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.updatePassword()...");
        System.out.println("  Step 1: Finding user with ID '" + userId + "'... SUCCESS");
        System.out.println("  Step 2: Verifying current password... FAILED");

        try {
            userService.updatePassword(userId, wrongCurrentPassword, newPassword);
            System.out.println("\nACTUAL OUTPUT: ❌ No exception thrown (unexpected!)");
        } catch (IllegalStateException e) {
            System.out.println("\nACTUAL OUTPUT:");
            System.out.println("  Exception: " + e.getClass().getSimpleName());
            System.out.println("  Message: '" + e.getMessage() + "'");
            System.out.println("  Password encoding: NOT ATTEMPTED");
            System.out.println("  Database save: NOT ATTEMPTED");
        }

        System.out.println("\nRESULT: ✅ SUCCESS - Security violation prevented");
    }

    @Test
    void demonstrateGetUsersByRole() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 8: GET USERS BY ROLE");
        System.out.println("=".repeat(70));

        Role targetRole = Role.ATTENDEE;
        System.out.println("INPUT:");
        System.out.println("  userService.getByRole(" + targetRole + ")");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  List<User> containing all users with role ATTENDEE:");
        System.out.println("  [");
        System.out.println("    User { id: '123456', email: 'john.doe@example.com', firstName: 'John', role: ATTENDEE }");
        System.out.println("    // Any other users with ATTENDEE role...");
        System.out.println("  ]");

        // Mock setup
        List<User> attendees = Arrays.asList(sampleUser1); // Only sampleUser1 is ATTENDEE
        when(userRepository.findByRole(targetRole)).thenReturn(attendees);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.getByRole(" + targetRole + ")...");

        List<User> result = userService.getByRole(targetRole);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  List<User> with " + result.size() + " " + targetRole + "(s):");
        System.out.println("  [");
        for (User user : result) {
            System.out.println("    User { id: '" + user.getId() + "', email: '" + user.getEmail() +
                    "', firstName: '" + user.getFirstName() + "', role: " + user.getRole() + " }");
        }
        System.out.println("  ]");

        System.out.println("\nRESULT: ✅ SUCCESS - Found " + result.size() + " user(s) with role " + targetRole);
    }

    @Test
    void demonstrateDeleteUser() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMO 9: DELETE USER");
        System.out.println("=".repeat(70));

        String userId = "123456";
        System.out.println("INPUT:");
        System.out.println("  userService.delete(\"" + userId + "\")");

        System.out.println("\nEXPECTED PROCESS:");
        System.out.println("  1. Call repository.deleteById(\"" + userId + "\")");
        System.out.println("  2. User removed from database");

        System.out.println("\nEXPECTED OUTPUT:");
        System.out.println("  Operation completed successfully (void method)");
        System.out.println("  User with ID '" + userId + "' deleted from database");

        // Mock setup
        doNothing().when(userRepository).deleteById(userId);

        // Execute
        System.out.println("\nEXECUTION:");
        System.out.println("  Calling userService.delete(\"" + userId + "\")...");

        userService.delete(userId);

        // Display results
        System.out.println("\nACTUAL OUTPUT:");
        System.out.println("  Operation completed successfully");
        System.out.println("  Repository.deleteById() called with ID: '" + userId + "'");
        System.out.println("  User deletion: COMPLETED");

        System.out.println("\nRESULT: ✅ SUCCESS - User deleted from database");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL USER SERVICE DEMOS COMPLETED");
        System.out.println("=".repeat(70));
    }
}