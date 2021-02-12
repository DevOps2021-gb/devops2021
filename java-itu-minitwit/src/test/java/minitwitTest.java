/*
import RoP.Result;
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

    //helperfunctions
    //Helper function to register a user
    Result<String> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2);
    }
    //login
    //logout
    //Records a message
    void add_message(String text) throws SQLException {
       */
/* var rs = Queries.add_message(text);
        if(rs == null) assert (false);
        else assert (rs>0);*//*

    }

    //tests:
    @Test
    void test_register(){
        String error = register("user1", "q123", null, null).get();
        assert (error=="");
        error = register("user1", "q123", null, null).get();
        assert (error=="The username is already taken");
        error = register("", "q123", null, null).get();
        assert (error=="You have to enter a username");
        error = register("user2", "", null, null).get();
        assert (error=="You have to enter a password");
        error = register("user2", "1", "2", null).get();
        assert (error=="The two passwords do not match");
        error = register("user2", "1", null, "bad email").get();
        assert (error=="You have to enter a valid email address");
    }
    @Test
    void test_message_recording() throws SQLException {
        //String error = register("foo", "default", null, null);       //todo uncomment to change test
        add_message("test message 1");
        add_message("<test message 2>");
        var rs = Queries.public_timeline();
        assert (rs.isSuccess());
        rs.get().next();        //todo find out why first result and second is text1
        var text1 = rs.get().getString("text");
        rs.get().next();
        var text2 = rs.get().getString("text");
        assert text1.equals("test message 1");
        assert text2.equals("<test message 2>"); //todo store as: "&lt;test message 2&gt;"
    }

    @Test
    void test_timelines(){

    }
}*/
