import RoP.Result;
import RoP.Success;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static spark.Spark.stop;

class queriesTest {
    String testDatabaseName = "testMinitwit";

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DB.setDATABASE(testDatabaseName);
        Queries.initDb();
        //awaitInitialization();
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
    //Helper function to register, login and get the id
    Result<Integer> register_login_getID(String username, String password, String password2, String email) {
        register(username, password, password2, email);
        login(username, password);
        var id = Queries.getUserId(username);
        assert (id.isSuccess());
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
        assert (rs.get());
    }

    //Tests


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
    void test_publicTimeline() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);

        String text1 = "test message 1", text2 = "<test message 2>";
        add_message(text1, id1.get());
        add_message(text2, id1.get());
        var rs = Queries.publicTimeline();
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        assert (tweet1.email.equals("foo@example.com"));
        assert (tweet1.username.equals("foo"));
        assert (tweet1.text.equals(text1));

        assert (tweet2.email.equals("foo@example.com"));
        assert (tweet2.username.equals("foo"));
        assert (tweet2.text.equals(text2));//todo store as: "&lt;test message 2&gt;"
    }

    @Test
    void test_getPersonalTweetsById() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());

        var rs = Queries.getPersonalTweetsById(id1.get());
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.email.equals("foo@example.com"));
        assert (tweet1.username.equals("foo"));
        assert (tweet1.text).equals("the message by foo");

        rs = Queries.getPersonalTweetsById(id2.get());
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.email.equals("bar@example.com"));
        assert (tweet2.username.equals("bar"));
        assert (tweet2.text.equals("the message by bar"));
    }

    @Test
    void test_getTweetsByUsername() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());

        var rs = Queries.getTweetsByUsername("foo");
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.email.equals("foo@example.com"));
        assert (tweet1.username.equals("foo"));
        assert (tweet1.text.equals("the message by foo"));

        rs = Queries.getTweetsByUsername("bar");
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.email.equals("bar@example.com"));
        assert (tweet2.username.equals("bar"));
        assert (tweet2.text.equals("the message by bar"));
    }

    @Test
    void test_queryGetUser() {
        var id1 = register_login_getID("foo", "default", null, "myEmail@itu.dk");
        var user1_1 = Queries.getUser("foo");
        var id1_rs = Queries.getUserId("foo");
        var user1_2 = Queries.getUserById(id1.get());

        assert (id1.get().equals(id1_rs.get()));
        assert (user1_1.get().id == id1.get());
        assert (user1_1.get().username.equals("foo"));
        assert (user1_1.get().pwHash.equals(Hashing.generatePasswordHash("default")));
        assert (user1_1.get().email.equals("myEmail@itu.dk"));
        assert (
                user1_1.get().id == user1_2.get().id &&
                user1_1.get().username.equals(user1_2.get().username) &&
                user1_1.get().pwHash.equals(user1_2.get().pwHash) &&
                user1_1.get().email.equals(user1_2.get().email));
    }


    @Test
    void test_getFollowing() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);
        var id2 = register_login_getID("bar","1234", null, null);
        var id3 = register_login_getID("brian","q123", null, null);

        var rs1 = Queries.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        assert (Queries.isFollowing(id1.get(), id2.get()).get());
        var rs2 = Queries.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());
        assert (Queries.isFollowing(id1.get(), id3.get()).get());

        var rs = Queries.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().get(0).username.equals("bar"));
        assert (rs.get().get(1).username.equals("brian"));
    }

    @Test
    void test_unfollowUser() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null, null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null, null);
        add_message("the message by bar", id2.get());

        var rs1 = Queries.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        var rs2 = Queries.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());


        assert (Queries.isFollowing(id1.get(), id2.get()).get());
        assert (Queries.isFollowing(id1.get(), id3.get()).get());

        var rsUnfollow1 = Queries.unfollowUser(id1.get(), "bar");
        assert (rsUnfollow1.isSuccess());
        assert (!Queries.isFollowing(id1.get(), id2.get()).get());
        var rsUnfollow2 = Queries.unfollowUser(id1.get(), "brian");
        assert (rsUnfollow2.isSuccess());
        assert (!Queries.isFollowing(id1.get(), id3.get()).get());

        var rs = Queries.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().size()==0);
    }


    @Test
    void test_following_PersonalTweets() throws SQLException {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null, null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null, null);
        add_message("the message by Biran v1", id3.get());
        add_message("the message by Biran v2", id3.get());


        var rTweets = Queries.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().size()==1);
        assert (rTweets.get().get(0).username.equals("foo")   && rTweets.get().get(0).text.equals("the message by foo"));

        var rs1 = Queries.followUser(id1.get(), "bar");
        var rs2 = Queries.followUser(id1.get(), "brian");
        assert (rs1.isSuccess() && rs2.isSuccess());

        rTweets = Queries.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().get(0).username.equals("brian") && rTweets.get().get(0).text.equals("the message by Biran v2"));
        assert (rTweets.get().get(1).username.equals("brian") && rTweets.get().get(1).text.equals("the message by Biran v1"));
        assert (rTweets.get().get(2).username.equals("bar")   && rTweets.get().get(2).text.equals("the message by bar"));
        assert (rTweets.get().get(3).username.equals("foo")   && rTweets.get().get(3).text.equals("the message by foo"));
    }

    //todo: queryLogin
    //todo: gravatarUrl

}
