
import logic.DB;
import logic.Hashing;
import logic.Logger;
import logic.Queries;
import rop.Result;
import rop.Success;
import org.junit.jupiter.api.Test;

import static spark.Spark.stop;

class QueriesTest {
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DB.setDATABASE("testMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            DB.setCONNECTIONSTRING(System.getProperty("DB_TEST_CONNECTION_STRING"));
            DB.setUSER(System.getProperty("DB_USER"));
            DB.setPW(System.getProperty("DB_PASSWORD"));
        }
        Queries.dropDB();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        stop();
    }

    //Helper functions

    //Helper function to register a user
    Result<String> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Queries.register(username, email, password, password2);
    }

    //Helper function to login
    Result<Boolean> login(String username, String password) {
        return Queries.queryLogin(username, password);
    }

    //Helper function to register and login in one go
    Result<Boolean> register_and_login(String username, String password) {
        register(username, password, null, null);
        return login(username, password);
    }
    int userId = 1;
    //Helper function to register, login and get the id
    Result<Integer> register_login_getID(String username, String password, String password2, String email) {
        register(username, password, password2, email);
        login(username, password);
        var id = Queries.getUserId(username);
        assert id.isSuccess();
        assert id.get() == userId;
        userId++;
        return id;
    }
    //Helper function to logout
    Result<Boolean> logout() {
        //todo logout helper function
        return new Success<>(true);
    }

    //Records a message
    void add_message(String text, int loggedInUserId) {
        var rs = Queries.addMessage(text, loggedInUserId);
        assert rs.get();
        try {
            Thread.sleep(100);      //hotfix: added to ensure order of messages
        } catch (InterruptedException e) { }
    }

    //Tests


    @Test
    void test_register(){
        Logger.processUsers();
        assert (int) Logger.getUsers() == 0;
        assert Queries.getCountUsers().get() == 0;

        var error = register("user1", "q123", null, null);
        assert error.isSuccess() && error.get().equals("OK");
        assert Queries.getCountUsers().get() == 1;
        Logger.processUsers();
        assert (int) Logger.getUsers() == 1;

        error = register("user1", "q123", null, null);
        assert !error.isSuccess() && error.getFailureMessage().equals("The username is already taken");
        error = register("", "q123", null, null);
        assert !error.isSuccess() && error.getFailureMessage().equals("You have to enter a username");
        error = register("user2", "", null, null);
        assert !error.isSuccess() && error.getFailureMessage().equals("You have to enter a password");
        error = register("user2", "1", "2", null);
        assert !error.isSuccess() && error.getFailureMessage().equals("The two passwords do not match");
        error = register("user2", "1", null, "bad email");
        assert !error.isSuccess() && error.getFailureMessage().equals("You have to enter a valid email address");
        assert Queries.getCountUsers().get() == 1;
        Logger.processUsers();
        assert (int) Logger.getUsers() == 1;
    }

    @Test
    void test_login_logout() {
        var result = register_and_login("user1", "default");
        assert result.isSuccess() && result.get();
        result = logout();
        assert result.isSuccess() && result.get(); //TODO will always succeed as is now
        result = login("user2", "wrongpassword");
        var msg = result.getFailureMessage();
        assert !result.isSuccess() && result.getFailureMessage().equals("Invalid username");
        result = login("user1", "wrongpassword");
        assert !result.isSuccess() && result.getFailureMessage().equals("Invalid password");
    }

    @Test
    void test_publicTimeline() {
        var id1 = register_login_getID("foo", "default", null, null);

        String text1 = "test message 1", text2 = "<test message 2>";
        add_message(text1, id1.get());
        add_message(text2, id1.get());
        var rs = Queries.publicTimeline();
        assert rs.isSuccess();
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        assert tweet1.getEmail().equals("foo@example.com");
        assert tweet1.getUsername().equals("foo");
        assert tweet1.getText().equals(text1);

        assert tweet2.getEmail().equals("foo@example.com");
        assert tweet2.getUsername().equals("foo");
        assert tweet2.getText().equals(text2);//todo store as: "&lt;test message 2&gt;"
    }

    @Test
    void test_getPersonalTweetsById() {
        assert Queries.getCountMessages().get() == 0;
        Logger.processMessages();
        assert (int) Logger.getMessages() == 0;

        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        assert Queries.getCountMessages().get() == 1;
        Logger.processMessages();
        assert (int) Logger.getMessages() == 1;


        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());
        assert Queries.getCountMessages().get() == 2;
        Logger.processMessages();
        assert (int) Logger.getMessages() == 2;

        var rs = Queries.getPersonalTweetsById(id1.get());
        assert rs.isSuccess();
        var tweet1 = rs.get().get(0);
        assert tweet1.getEmail().equals("foo@example.com");
        assert tweet1.getUsername().equals("foo");
        assert tweet1.getText().equals("the message by foo");

        rs = Queries.getPersonalTweetsById(id2.get());
        assert rs.isSuccess();
        var tweet2 = rs.get().get(0);
        assert tweet2.getEmail().equals("bar@example.com");
        assert tweet2.getUsername().equals("bar");
        assert tweet2.getText().equals("the message by bar");
    }

    @Test
    void test_getTweetsByUsername() {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());

        var rs = Queries.getTweetsByUsername("foo");
        assert rs.isSuccess();
        var tweet1 = rs.get().get(0);
        assert tweet1.getEmail().equals("foo@example.com");
        assert tweet1.getUsername().equals("foo");
        assert tweet1.getText().equals("the message by foo");

        rs = Queries.getTweetsByUsername("bar");
        assert rs.isSuccess();
        var tweet2 = rs.get().get(0);
        assert tweet2.getEmail().equals("bar@example.com");
        assert tweet2.getUsername().equals("bar");
        assert tweet2.getText().equals("the message by bar");
    }

    @Test
    void test_queryGetUser() {
        var id1 = register_login_getID("foo", "default", null, "myEmail@itu.dk");
        var user1_1 = Queries.getUser("foo");
        var id1_rs = Queries.getUserId("foo");
        var user1_2 = Queries.getUserById(id1.get());

        assert id1.get().equals(id1_rs.get());
        assert user1_1.get().id == id1.get();
        assert user1_1.get().getUsername().equals("foo");
        assert user1_1.get().getPwHash().equals(Hashing.generatePasswordHash("default"));
        assert user1_1.get().getEmail().equals("myEmail@itu.dk");
        assert user1_1.get().id == user1_2.get().id
                        && user1_1.get().getUsername().equals(user1_2.get().getUsername())
                        && user1_1.get().getPwHash().equals(user1_2.get().getPwHash())
                        && user1_1.get().getEmail().equals(user1_2.get().getEmail());
    }


    @Test
    void test_getFollowing() {
        var id1 = register_login_getID("foo", "default", null, null);
        var id2 = register_login_getID("bar","1234", null, null);
        var id3 = register_login_getID("brian","q123", null, null);


        assert Queries.getCountFollowers().get() == 0;
        Logger.processFollowers();
        assert (int) Logger.getFollowers() == 0;

        var rs1 = Queries.followUser(id1.get(), "bar");
        assert rs1.isSuccess();
        assert Queries.isFollowing(id1.get(), id2.get()).get();
        assert Queries.getCountFollowers().get() == 1;
        Logger.processFollowers();
        assert (int) Logger.getFollowers() == 1;

        var rs2 = Queries.followUser(id1.get(), "brian");
        assert rs2.isSuccess();
        assert Queries.isFollowing(id1.get(), id3.get()).get();
        assert Queries.getCountFollowers().get() == 2;
        Logger.processFollowers();
        assert (int) Logger.getFollowers() == 2;

        var rs = Queries.getFollowing(id1.get());
        assert rs.isSuccess();
        assert rs.get().get(0).getUsername().equals("bar");
        assert rs.get().get(1).getUsername().equals("brian");
    }

    @Test
    void test_unfollowUser() {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null, null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null, null);
        add_message("the message by bar", id2.get());

        var rs1 = Queries.followUser(id1.get(), "bar");
        assert rs1.isSuccess();
        var rs2 = Queries.followUser(id1.get(), "brian");
        assert rs2.isSuccess();


        assert Queries.isFollowing(id1.get(), id2.get()).get();
        assert Queries.isFollowing(id1.get(), id3.get()).get();

        var rsUnfollow1 = Queries.unfollowUser(id1.get(), "bar");
        assert rsUnfollow1.isSuccess();
        assert !Queries.isFollowing(id1.get(), id2.get()).get();
        var rsUnfollow2 = Queries.unfollowUser(id1.get(), "brian");
        assert rsUnfollow2.isSuccess();
        assert !Queries.isFollowing(id1.get(), id3.get()).get();

        var rs = Queries.getFollowing(id1.get());
        assert rs.isSuccess();
        assert rs.get().size()==0;
    }


    @Test
    void test_following_PersonalTweets() {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null, null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null, null);
        add_message("the message by Biran v1", id3.get());
        add_message("the message by Biran v2", id3.get());


        var rTweets = Queries.getPersonalTweetsById(id1.get());
        assert rTweets.isSuccess();
        assert rTweets.get().size()==1;
        assert rTweets.get().get(0).getUsername().equals("foo")   && rTweets.get().get(0).getText().equals("the message by foo");

        var rs1 = Queries.followUser(id1.get(), "bar");
        var rs2 = Queries.followUser(id1.get(), "brian");
        assert rs1.isSuccess() && rs2.isSuccess();

        rTweets = Queries.getPersonalTweetsById(id1.get());
        assert rTweets.isSuccess();
        assert rTweets.get().get(0).getUsername().equals("brian") && rTweets.get().get(0).getText().equals("the message by Biran v2");
        assert rTweets.get().get(1).getUsername().equals("brian") && rTweets.get().get(1).getText().equals("the message by Biran v1");
        assert rTweets.get().get(2).getUsername().equals("bar")   && rTweets.get().get(2).getText().equals("the message by bar");
        assert rTweets.get().get(3).getUsername().equals("foo")   && rTweets.get().get(3).getText().equals("the message by foo");
    }

    //todo: queryLogin
    //todo: gravatarUrl

}
