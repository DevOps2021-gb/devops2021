import Logic.Logger;
import Persistence.Repositories;
import org.junit.jupiter.api.Test;

public class MinitwitTests extends DatabaseTestBase {
    @Test
    void test_register(){
        Logger.processUsers();
        assert ((int) Logger.getUsers() == 0);
        assert (Repositories.countUsers().get() == 0);

        var error = register("user1", "q123", null, null);
        assert (error.isSuccess() && error.get().equals("OK"));
        assert (Repositories.countUsers().get() == 1);
        Logger.processUsers();
        assert ((int) Logger.getUsers() == 1);

        error = register("user1", "q123", null, null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("The username is already taken"));
        error = register("", "q123", null, null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a username"));
        error = register("user2", "", null, null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a password"));
        error = register("user2", "1", "2", null);
        assert (!error.isSuccess() && error.getFailureMessage().equals("The two passwords do not match"));
        error = register("user2", "1", null, "bad email");
        assert (!error.isSuccess() && error.getFailureMessage().equals("You have to enter a valid email address"));
        assert (Repositories.countUsers().get() == 1);
        Logger.processUsers();
        assert ((int) Logger.getUsers() == 1);
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
        var id1 = register_login_getID("foo", "default", null, null);

        String text1 = "test message 1", text2 = "<test message 2>";
        add_message(text1, id1.get());
        add_message(text2, id1.get());
        var rs = Repositories.publicTimeline();
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
