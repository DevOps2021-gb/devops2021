import services.LogService;
import persistence.MessageRepository;
import persistence.UserRepository;
import services.UserService;
import org.junit.jupiter.api.Test;

class MessageServiceTests extends DatabaseTestBase {
    @Test
    void validateUserCredentials_given_already_existing_user_returns_user_already_exists() {
        var result = UserRepository.addUser("user1", "test@test.dk", "q123");
        assert (result.isSuccess() && result.get().equals("OK"));
        assert (UserRepository.countUsers().get() == 1);
        LogService.processUsers();
        assert ((int) LogService.getUsers() == 1);

        result = UserService.validateUserCredentials("user1", "test@test.dk", "q123", "q123");
        assert (!result.isSuccess() && result.getFailureMessage().equals("The username is already taken"));
    }

    @Test
    void validateUserCredentials_given_no_username_returns_missing_username() {
        var error = UserService.validateUserCredentials("", "q123", null, null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a username"));
    }

    @Test
    void validateUserCredentials_given_no_password_returns_missing_password() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "", null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a password"));
    }

    @Test
    void validateUserCredentials_given_non_matching_passwords_returns_non_matching_passwords() {
        var error = UserService.validateUserCredentials("user2", "test@test.dk", "2", "1");
        assert (!error.isSuccess() && error.getFailureMessage().equals("The two passwords do not match"));
    }

    @Test
    void validateUserCredentials_given_invalid_email_returns_invalid_email() {
        var error = UserService.validateUserCredentials("user2", "1", null, "bad email");
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a valid email address"));
    }

    @Test
    void test_login_logout() {
        var result = register_and_login("user1", "default");
        assert (result.isSuccess() && result.get());
        result = logout();
        assert (result.isSuccess() && result.get()); //TODO will always succeed as is now
        result = login("user2", "wrongpassword");
        var msg = result.getFailureMessage();
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid username"));
        result = login("user1", "wrongpassword");
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid password"));
    }

    @Test
    void test_publicTimeline() {
        var id1 = register_login_getID("foo", "default", null);

        String text1 = "test message 1", text2 = "<test message 2>";
        add_message(text1, id1.get());
        add_message(text2, id1.get());
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
