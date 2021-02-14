import RoP.Result;
import RoP.Success;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static spark.Spark.stop;

class minitwitTest {
    File databaseFile;
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        try {
            databaseFile = File.createTempFile("testDB-", ".db");
            Queries.setDATABASE(databaseFile.getName());
            Queries.initDb();
            //awaitInitialization();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        databaseFile.delete();      //todo: findout why it doesn't delete the file
        stop();
    }

    /* Helper functions */

    //Helper function to register a user
    Result<String> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2);
    }

    //Helper function to login
    Result<String> login(String username, String password) {
        return Queries.queryLogin(username, password);
    }

    //Helper function to register and login in one go
    Result<String> register_and_login(String username, String password) {
        register(username, password, null, null);
        return login(username, password);
    }

    //Helper function to logout
    Result<String> logout() {
        //todo logout helper function
        return new Success<>("");
    }

    //Records a message
    void add_message(String text) throws SQLException {
        var rs = Queries.addMessage(text, 1);
        if(rs == null) assert (false);
        else assert (rs.get() > 0);
    }

    /* Tests */

    @Test
    void test_register(){
        var error = register("user1", "q123", null, null);
        assert (error.isSuccess() && error.get().equals("OK"));
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
    }

    @Test
    void test_login_logout() {
        var result = register_and_login("user1", "default");
        assert (result.isSuccess() && result.get().equals("login successful"));
        result = logout();
        assert (result.isSuccess() && result.get().equals("")); //TODO will always succeed as is now
        result = login("user2", "wrongpassword");
        var msg = result.getFailureMessage();
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid username"));
        result = login("user1", "wrongpassword");
        assert (!result.isSuccess() && result.getFailureMessage().equals("Invalid password"));
    }

    @Test
    void test_message_recording() throws SQLException {
        register("foo", "default", null, null);
        String text1 = "test message 1", text2 = "<test message 2>";
        add_message(text1);
        add_message(text2);
        var rs = Queries.publicTimeline();
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        assert (tweet1.email().equals("foo@example.com"));
        assert (tweet1.username().equals("foo"));
        assert (tweet1.text().equals(text1));

        assert (tweet2.email().equals("foo@example.com"));
        assert (tweet2.username().equals("foo"));
        assert (tweet2.text().equals(text2));//todo store as: "&lt;test message 2&gt;"
    }

    @Test
    void test_timelines() throws SQLException {
        register_and_login("foo", "default");
        add_message("the message by foo");
        logout();
        register_and_login("bar","default");
        add_message("the message by bar");
        // TODO finish
    }
}