import org.junit.jupiter.api.Assertions;
import services.LogService;
import persistence.MessageRepository;
import persistence.UserRepository;
import services.UserService;
import org.junit.jupiter.api.Test;

class MessageServiceTests extends DatabaseTestBase {
    @Test
    void validateUserCredentialsGivenAlreadyExistingUserReturnsUserAlreadyExists() {
        var result = UserRepository.addUser("user1", "test@test.dk", "q123");
        Assertions.assertEquals(true, result.isSuccess());
        Assertions.assertEquals("OK", result.get());
        Assertions.assertEquals(true, UserRepository.countUsers().get() == 1);
        LogService.processUsers();
        Assertions.assertEquals(true, LogService.getUsers() == 1);

        result = UserService.validateUserCredentials("user1", "test@test.dk", "q123", "q123");
        Assertions.assertEquals(false, result.isSuccess());
        Assertions.assertEquals("The username is already taken",result.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoUsernameReturnsMissingUsername() {
        var error = UserService.validateUserCredentials("", "q123", null, null);
        Assertions.assertEquals(false, error.isSuccess());
        Assertions.assertEquals("You have to enter a username", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNoPasswordReturnsMissingPassword() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "", null);
        Assertions.assertEquals(false, error.isSuccess());
        Assertions.assertEquals("You have to enter a password", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenNonMatchingPasswordsReturnsNonMatchingPasswords() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "2", "1");
        Assertions.assertEquals(false, error.isSuccess());
        Assertions.assertEquals("The two passwords do not match", error.getFailureMessage());
    }

    @Test
    void validateUserCredentialsGivenInvalidEmailReturnsInvalidEmail() {
        var error = UserService.validateUserCredentials("user2", "1", null, "bad email");
        Assertions.assertEquals(false, error.isSuccess());
        Assertions.assertEquals("You have to enter a valid email address", error.getFailureMessage());
    }

    @Test
    void testLoginLogout() {
        var result = this.registerAndLogin("user1", "default");
        Assertions.assertEquals(true, result.isSuccess());
        Assertions.assertEquals(true, result.get());
        result = this.logout();
        Assertions.assertEquals(true, result.isSuccess());
        Assertions.assertEquals(true, result.get()); //TODO will always succeed as is now
        result = this.login("user2", "wrongpassword");
        Assertions.assertEquals(false, result.isSuccess());
        Assertions.assertEquals("Invalid username", result.getFailureMessage());
        result = this.login("user1", "wrongpassword");
        Assertions.assertEquals(false, result.isSuccess());
        Assertions.assertEquals("Invalid password", result.getFailureMessage());
    }

    @Test
    void testPublicTimeline() {
        var id1 = this.registerLoginGetID("foo", "default", null);
        String text1 = "test message 1", text2 = "<test message 2>";
        this.addMessage(text1, id1.get());
        this.addMessage(text2, id1.get());
        var rs = MessageRepository.publicTimeline();
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        Assertions.assertEquals( "foo@example.com", tweet1.getEmail());
        Assertions.assertEquals( "foo", tweet1.getUsername());
        Assertions.assertEquals( text1, tweet1.getText());
        Assertions.assertEquals( "foo@example.com", tweet2.getEmail());
        Assertions.assertEquals( "foo", tweet2.getUsername());
        //todo store as: "&lt;test message 2&gt;"
        Assertions.assertEquals(text2, tweet2.getText());
    }
}
