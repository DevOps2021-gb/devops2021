import services.LogService;
import persistence.FollowerRepository;
import persistence.MessageRepository;
import org.junit.jupiter.api.Test;

class FollowerTests extends DatabaseTestBase{
    @Test
    void test_getFollowing() {
        var id1 = register_login_getID("foo", "default",  null);
        var id2 = register_login_getID("bar","1234",  null);
        var id3 = register_login_getID("brian","q123",  null);


        assert (FollowerRepository.countFollowers().get() == 0);
        LogService.processFollowers();
        assert ((int) LogService.getFollowers() == 0);

        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        assert (FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        assert (FollowerRepository.countFollowers().get() == 1);
        LogService.processFollowers();
        assert ((int) LogService.getFollowers() == 1);

        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());
        assert (FollowerRepository.isFollowing(id1.get(), id3.get()).get());
        assert (FollowerRepository.countFollowers().get() == 2);
        LogService.processFollowers();
        assert ((int) LogService.getFollowers() == 2);

        var rs = FollowerRepository.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().get(0).getUsername().equals("bar"));
        assert (rs.get().get(1).getUsername().equals("brian"));
    }

    @Test
    void test_unfollowUser() {
        var id1 = register_login_getID("foo", "default", null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null);
        add_message("the message by bar", id2.get());

        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        assert (rs1.isSuccess());
        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        assert (rs2.isSuccess());


        assert (FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        assert (FollowerRepository.isFollowing(id1.get(), id3.get()).get());

        var rsUnfollow1 = FollowerRepository.unfollowUser(id1.get(), "bar");
        assert (rsUnfollow1.isSuccess());
        assert (!FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        var rsUnfollow2 = FollowerRepository.unfollowUser(id1.get(), "brian");
        assert (rsUnfollow2.isSuccess());
        assert (!FollowerRepository.isFollowing(id1.get(), id3.get()).get());

        var rs = FollowerRepository.getFollowing(id1.get());
        assert (rs.isSuccess());
        assert (rs.get().size()==0);
    }


    @Test
    void test_following_PersonalTweets() {
        var id1 = register_login_getID("foo", "default", null);
        add_message("the message by foo", id1.get());
        var id2 = register_login_getID("bar","1234", null);
        add_message("the message by bar", id2.get());
        var id3 = register_login_getID("brian","q123", null);
        add_message("the message by Biran v1", id3.get());
        add_message("the message by Biran v2", id3.get());


        var rTweets = MessageRepository.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().size()==1);
        assert (rTweets.get().get(0).getUsername().equals("foo")   && rTweets.get().get(0).getText().equals("the message by foo"));

        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        assert (rs1.isSuccess() && rs2.isSuccess());

        rTweets = MessageRepository.getPersonalTweetsById(id1.get());
        assert (rTweets.isSuccess());
        assert (rTweets.get().get(0).getUsername().equals("brian") && rTweets.get().get(0).getText().equals("the message by Biran v2"));
        assert (rTweets.get().get(1).getUsername().equals("brian") && rTweets.get().get(1).getText().equals("the message by Biran v1"));
        assert (rTweets.get().get(2).getUsername().equals("bar")   && rTweets.get().get(2).getText().equals("the message by bar"));
        assert (rTweets.get().get(3).getUsername().equals("foo")   && rTweets.get().get(3).getText().equals("the message by foo"));
    }

}
