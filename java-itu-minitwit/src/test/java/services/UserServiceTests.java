package services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.UserRepository;
import testUtilities.DatabaseTestBase;

public class UserServiceTests extends DatabaseTestBase {
    @Test
    void validateUserCredentialsGivenAlreadyExistingUserReturnsUserAlreadyExists() {
        var result = UserRepository.addUser("user1", "test@test.dk", "q123");
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("OK", result.get());
        Assertions.assertEquals((long) UserRepository.countUsers().get(), 1);
        LogService.processUsers();
        Assertions.assertEquals(1, LogService.getUsers());

        result = UserService.validateUserCredentials("user1", "test@test.dk", "q123", "q123");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("The username is already taken",result.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoUsernameReturnsMissingUsername() {
        var error = UserService.validateUserCredentials("", "q123", null, null);
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a username", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoPasswordReturnsMissingPassword() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "", null);
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a password", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNonMatchingPasswordsReturnsNonMatchingPasswords() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "2", "1");
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("The two passwords do not match", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenInvalidEmailReturnsInvalidEmail() {
        var error = UserService.validateUserCredentials("user2", "1", null, "bad email");
        Assertions.assertFalse(error.isSuccess());
        Assertions.assertEquals("You have to enter a valid email address", error.getFailureMessage());
    }
}
