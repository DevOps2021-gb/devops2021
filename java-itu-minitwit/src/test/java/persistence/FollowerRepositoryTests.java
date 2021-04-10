package persistence;

import org.junit.jupiter.api.Assertions;
import services.MaintenanceService;
import org.junit.jupiter.api.Test;
import testUtilities.DatabaseTestBase;

class FollowerRepositoryTests extends DatabaseTestBase {
    @Test
    void testGetFollowing() {
        var id1 = this.registerLoginGetID("foo", "default",  null);
        var id2 = this.registerLoginGetID("bar", "1234",  null);
        var id3 = this.registerLoginGetID("brian", "q123",  null);
        Assertions.assertEquals(0, (long) FollowerRepository.countFollowers().get());
        MaintenanceService.processFollowers();
        Assertions.assertEquals(0, MaintenanceService.getFollowers());
        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        Assertions.assertTrue(rs1.isSuccess());
        Assertions.assertEquals(true, FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        Assertions.assertEquals(1, (long) FollowerRepository.countFollowers().get());
        MaintenanceService.processFollowers();
        Assertions.assertEquals(1, MaintenanceService.getFollowers());
        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        Assertions.assertTrue(rs2.isSuccess());
        Assertions.assertEquals(true, FollowerRepository.isFollowing(id1.get(), id3.get()).get());
        Assertions.assertEquals(2, (long) FollowerRepository.countFollowers().get());
        MaintenanceService.processFollowers();
        Assertions.assertEquals(2, MaintenanceService.getFollowers());
        var rs = FollowerRepository.getFollowing(id1.get());
        Assertions.assertTrue(rs.isSuccess());
        Assertions.assertEquals("bar", rs.get().get(0).getUsername());
        Assertions.assertEquals("brian", rs.get().get(1).getUsername());
    }

    @Test
    void testUnfollowUser() {
        var id1 = this.registerLoginGetID("foo", "default", null);
        this.addMessage("the message by foo", id1.get());
        var id2 = this.registerLoginGetID("bar", "1234", null);
        this.addMessage("the message by bar", id2.get());
        var id3 = this.registerLoginGetID("brian", "q123", null);
        this.addMessage("the message by bar", id2.get());
        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        Assertions.assertTrue(rs1.isSuccess());
        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        Assertions.assertTrue(rs2.isSuccess());
        Assertions.assertEquals(true, FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        Assertions.assertEquals(true, FollowerRepository.isFollowing(id1.get(), id3.get()).get());
        var rsUnfollow1 = FollowerRepository.unfollowUser(id1.get(), "bar");
        Assertions.assertTrue(rsUnfollow1.isSuccess());
        Assertions.assertFalse(FollowerRepository.isFollowing(id1.get(), id2.get()).get());
        var rsUnfollow2 = FollowerRepository.unfollowUser(id1.get(), "brian");
        Assertions.assertTrue(rsUnfollow2.isSuccess());
        Assertions.assertFalse(FollowerRepository.isFollowing(id1.get(), id3.get()).get());
        var rs = FollowerRepository.getFollowing(id1.get());
        Assertions.assertTrue(rs.isSuccess());
        Assertions.assertEquals(0, rs.get().size());
    }

    @Test
    void testFollowingPersonalTweets() {
        var id1 = this.registerLoginGetID("foo", "default", null);
        this.addMessage("the message by foo", id1.get());
        var id2 = this.registerLoginGetID("bar", "1234", null);
        this.addMessage("the message by bar", id2.get());
        var id3 = this.registerLoginGetID("brian", "q123", null);
        this.addMessage("the message by Biran v1", id3.get());
        this.addMessage("the message by Biran v2", id3.get());
        var rTweets = MessageRepository.getPersonalTweetsById(id1.get());
        Assertions.assertTrue(rTweets.isSuccess());
        Assertions.assertEquals(1, rTweets.get().size());
        Assertions.assertEquals("foo", rTweets.get().get(0).getUsername());
        Assertions.assertEquals("the message by foo", rTweets.get().get(0).getText());
        var rs1 = FollowerRepository.followUser(id1.get(), "bar");
        var rs2 = FollowerRepository.followUser(id1.get(), "brian");
        Assertions.assertTrue(rs1.isSuccess());
        Assertions.assertTrue(rs2.isSuccess());
        rTweets = MessageRepository.getPersonalTweetsById(id1.get());
        Assertions.assertTrue(rTweets.isSuccess());
        Assertions.assertEquals("brian", rTweets.get().get(0).getUsername());
        Assertions.assertEquals("the message by Biran v2", rTweets.get().get(0).getText());
        Assertions.assertEquals("brian", rTweets.get().get(1).getUsername());
        Assertions.assertEquals("the message by Biran v1", rTweets.get().get(1).getText());
        Assertions.assertEquals("bar", rTweets.get().get(2).getUsername());
        Assertions.assertEquals("the message by bar", rTweets.get().get(2).getText());
        Assertions.assertEquals("foo", rTweets.get().get(3).getUsername());
        Assertions.assertEquals("the message by foo", rTweets.get().get(3).getText());
    }
}