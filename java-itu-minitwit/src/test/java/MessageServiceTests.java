
import services.LogService;
import persistence.MessageRepository;
import persistence.UserRepository;
import services.UserService;
import org.junit.jupiter.api.Test;

class MessageServiceTests extends DatabaseTestBase {
    @Test
    void validateUserCredentialsGivenAlreadyExistingUserReturnsUserAlreadyExists() {
        var result = UserRepository.addUser("user1", "test@test.dk", "q123");
        assert (result.isSuccess() && result.get().equals("OK"));
        assert (UserRepository.countUsers().get() == 1);
        LogService.processUsers();
        assert ((int) LogService.getUsers() == 1);

        result = UserService.validateUserCredentials("user1", "test@test.dk", "q123", "q123");
        assert (!result.isSuccess() && result.getFailureMessage().equals("The username is already taken"));
    }

    @Test
    void validateUserCredentialsGivenNoUsernameReturnsMissingUsername() {
        var error = UserService.validateUserCredentials("", "q123", null, null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a username"));
    }

    @Test
    void validateUserCredentialsGivenNoPasswordReturnsMissingPassword() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "", null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a password"));
    }

    @Test
    void validateUserCredentialsGivenNonMatchingPasswordsReturnsNonMatchingPasswords() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "2", "1");
        assert (!error.isSuccess() && error.getFailureMessage().equals("The two passwords do not match"));
    }

    @Test
    void validateUserCredentialsGivenInvalidEmailReturnsInvalidEmail() {
        var error = UserService.validateUserCredentials("user2", "1", null, "bad email");
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a valid email address"));
    }

    @Test
    void testLoginLogout() {
        var result = this.registerAndLogin("user1", "default");
        assert (result.isSuccess() && result.get());
        result = logout();
        assert (result.isSuccess() && result.get()); //TODO will always succeed as is now
        result = login("user2", "wrongpassword");
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid username"));
        result = login("user1", "wrongpassword");
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid password"));
    }

    @Test
    void testPublicTimeline() {
        var id1 = this.registerLoginGetID("foo", "default", null);
        String text1 = "test message 1", text2 = "<test message 2>";
        this.addMessage(text1, id1.get());
        this.addMessage(text2, id1.get());
        var rs = MessageRepository.publicTimeline();
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        assert (tweet1.getEmail().equals("foo@example.com"));
        assert (tweet1.getUsername().equals("foo"));
        assert (tweet1.getText().equals(text1));
        assert (tweet2.getEmail().equals("foo@example.com"));
        assert (tweet2.getUsername().equals("foo"));
        assert (tweet2.getText().equals(text2));//todo store as: "&lt;test message 2&gt;"
    }
}
