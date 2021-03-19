import Logic.Logger;
import Persistence.Repositories;
import Utilities.Hashing;
import org.junit.jupiter.api.Test;

class RepositoriesTests extends DatabaseTestBase{
    @Test
    void test_getPersonalTweetsById() {
        assert (Repositories.countMessages().get() == 0);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 0);

        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        assert (Repositories.countMessages().get() == 1);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 1);


        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());
        assert (Repositories.countMessages().get() == 2);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 2);

        var rs = Repositories.getPersonalTweetsById(id1.get());
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.getEmail().equals("foo@example.com"));
        assert (tweet1.getUsername().equals("foo"));
        assert (tweet1.getText()).equals("the message by foo");

        rs = Repositories.getPersonalTweetsById(id2.get());
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.getEmail().equals("bar@example.com"));
        assert (tweet2.getUsername().equals("bar"));
        assert (tweet2.getText().equals("the message by bar"));
    }

    @Test
    void test_getTweetsByUsername() {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        logout();
        var id2 = register_login_getID("bar","default", null, null);
        add_message("the message by bar", id2.get());

        var rs = Repositories.getTweetsByUsername("foo");
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.getEmail().equals("foo@example.com"));
        assert (tweet1.getUsername().equals("foo"));
        assert (tweet1.getText().equals("the message by foo"));

        rs = Repositories.getTweetsByUsername("bar");
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.getEmail().equals("bar@example.com"));
        assert (tweet2.getUsername().equals("bar"));
        assert (tweet2.getText().equals("the message by bar"));
    }

    @Test
    void test_queryGetUser() {
        var id1 = register_login_getID("foo", "default", null, "myEmail@itu.dk");
        var user1_1 = Repositories.getUser("foo");
        var id1_rs = Repositories.getUserId("foo");
        var user1_2 = Repositories.getUserById(id1.get());

        assert (id1.get().equals(id1_rs.get()));
        assert (user1_1.get().id == id1.get());
        assert (user1_1.get().getUsername().equals("foo"));
        assert (user1_1.get().getPwHash().equals(Hashing.generatePasswordHash("default")));
        assert (user1_1.get().getEmail().equals("myEmail@itu.dk"));
        assert (
                user1_1.get().id == user1_2.get().id &&
                user1_1.get().getUsername().equals(user1_2.get().getUsername()) &&
                user1_1.get().getPwHash().equals(user1_2.get().getPwHash()) &&
                user1_1.get().getEmail().equals(user1_2.get().getEmail()));
    }


    @Test
    void test_getFollowing() {
        var id1 = register_login_getID("foo", "default", null, null);
        var id2 = register_login_getID("bar","1234", null, null);
        var id3 = register_login_getID("brian","q123", null, null);


        assert (Repositories.countFollowers().get() == 0);
        Logger.processFollowers();
        assert ((int) Logger.getFollowers() == 0);

        var rs1 = Repositories.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        assert (Repositories.isFollowing(id1.get(), id2.get()).get());
        assert (Repositories.countFollowers().get() == 1);
        Logger.processFollowers();
        assert ((int) Logger.getFollowers() == 1);

        var rs2 = Repositories.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());
        assert (Repositories.isFollowing(id1.get(), id3.get()).get());
        assert (Repositories.countFollowers().get() == 2);
        Logger.processFollowers();
        assert ((int) Logger.getFollowers() == 2);

        var rs = Repositories.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().get(0).getUsername().equals("bar"));
        assert (rs.get().get(1).getUsername().equals("brian"));
    }

    @Test
    void test_unfollowUser() {
        var id1 = register_login_getID("foo", "default", null, null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null, null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null, null);
        add_message("the message by bar", id2.get());

        var rs1 = Repositories.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        var rs2 = Repositories.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());


        assert (Repositories.isFollowing(id1.get(), id2.get()).get());
        assert (Repositories.isFollowing(id1.get(), id3.get()).get());

        var rsUnfollow1 = Repositories.unfollowUser(id1.get(), "bar");
        assert (rsUnfollow1.isSuccess());
        assert (!Repositories.isFollowing(id1.get(), id2.get()).get());
        var rsUnfollow2 = Repositories.unfollowUser(id1.get(), "brian");
        assert (rsUnfollow2.isSuccess());
        assert (!Repositories.isFollowing(id1.get(), id3.get()).get());

        var rs = Repositories.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().size()==0);
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


        var rTweets = Repositories.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().size()==1);
        assert (rTweets.get().get(0).getUsername().equals("foo")   && rTweets.get().get(0).getText().equals("the message by foo"));

        var rs1 = Repositories.followUser(id1.get(), "bar");
        var rs2 = Repositories.followUser(id1.get(), "brian");
        assert (rs1.isSuccess() && rs2.isSuccess());

        rTweets = Repositories.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().get(0).getUsername().equals("brian") && rTweets.get().get(0).getText().equals("the message by Biran v2"));
        assert (rTweets.get().get(1).getUsername().equals("brian") && rTweets.get().get(1).getText().equals("the message by Biran v1"));
        assert (rTweets.get().get(2).getUsername().equals("bar")   && rTweets.get().get(2).getText().equals("the message by bar"));
        assert (rTweets.get().get(3).getUsername().equals("foo")   && rTweets.get().get(3).getText().equals("the message by foo"));
    }

    //todo: queryLogin
    //todo: gravatarUrl

}
