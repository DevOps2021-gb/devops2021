import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

class minitwitTest {
    File databaseFile;
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        try {
            databaseFile = File.createTempFile("testDB-", ".db");
            Queries.setDATABASE(databaseFile.getName());
            Queries.init_db();
            //Queries.session = new Session(Queries.connect_db(), new User("testUsername"));
            //awaitInitialization();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        databaseFile.delete();
        stop();
    }

    /* Helper functions */

    //Helper function to register a user
    String register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2).get();
    }

    //Helper function to login
    String login(String username, String password) {
        return Queries.queryLogin(username, password).get();
    }

    //Helper function to register and login in one go
    String register_and_login(String username, String password) {
        register(username, password, null, null);
        return login(username, password);
    }

    //Helper function to logout
    String logout() {
        //todo logout helper function
        return "";
    }

    //Records a message
    void add_message(String text) throws SQLException {
        var rs = Queries.add_message(text, 1);
        if(rs == null) assert (false);
        else assert (rs.get() > 0);
    }

    /* Tests */

    @Test
    void test_register(){
        String error = register("user1", "q123", null, null);
        assert (error.equals("OK"));
        error = register("user1", "q123", null, null);
        assert (error.equals("The username is already taken"));
        error = register("", "q123", null, null);
        assert (error.equals("You have to enter a username"));
        error = register("user2", "", null, null);
        assert (error.equals("You have to enter a password"));
        error = register("user2", "1", "2", null);
        assert (error.equals("The two passwords do not match"));
        error = register("user2", "1", null, "bad email");
        assert (error.equals("You have to enter a valid email address"));
    }

    @Test
    void test_login_logout() {
        String error = register_and_login("user1", "default");
        assert (error.equals("login successful"));
        error = logout();
        assert (error.equals("")); //TODO will always succeed as is now
        error = login("user2", "wrongpassword");
        assert (error.equals("Invalid username"));
        error = login("user1", "wrongpassword");
        assert (error.equals("Invalid password"));
    }

    @Test
    void test_message_recording() throws SQLException {
        //String error = register("foo", "default", null, null);       //todo uncomment to change test
        add_message("test message 1");
        add_message("<test message 2>");
        var rs = Queries.public_timeline();
        assert (rs.isSuccess());
        //rs.get().next();        //todo find out why first result and second is text1
        var text1 = rs.get().get(0).text();
        //rs.get().next();
        var text2 = rs.get().get(1).text();
        assert text1.equals("test message 1");
        assert text2.equals("<test message 2>"); //todo store as: "&lt;test message 2&gt;"
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