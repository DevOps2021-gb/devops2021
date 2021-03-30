package services;

import org.junit.jupiter.api.Assertions;
import persistence.MessageRepository;
import org.junit.jupiter.api.Test;
import testUtilities.DatabaseTestBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class MessageServiceTests extends DatabaseTestBase {
    @Test
    void testLoginLogout() {
        var result = this.registerAndLogin("user1", "default");
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.get());
        result = this.logout();
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertTrue(result.get()); //TODO will always succeed as is now
        result = this.login("user2", "wrongpassword");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Invalid username", result.getFailureMessage());
        result = this.login("user1", "wrongpassword");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals("Invalid password", result.getFailureMessage());
    }

    @Test
    void testPublicTimeline() {
        var id1 = this.registerLoginGetID("foo", "default", null);
        String text1 = "test message 1", text2 = "<test message 2>";
        this.addMessage(text1, id1.get());
        this.addMessage(text2, id1.get());
        var rs = MessageRepository.publicTimeline();
        Assertions.assertTrue(rs.isSuccess());
        var tweet1 = rs.get().get(1);
        var tweet2 = rs.get().get(0);
        Assertions.assertEquals( "foo@example.com", tweet1.getEmail());
        Assertions.assertEquals( "foo", tweet1.getUsername());
        Assertions.assertEquals( text1, tweet1.getText());
        Assertions.assertEquals( "foo@example.com", tweet2.getEmail());
        Assertions.assertEquals( "foo", tweet2.getUsername());
        //todo store as: "&lt;test message 2&gt;"
        Assertions.assertEquals(text2, tweet2.getText());
    }

    @Test
    void tweetsFromListOfHashMapGivenHashmapReturnsTweets() {
        HashMap hm = new HashMap();
        hm.put("email", "test@test.dk");
        hm.put("username", "test");
        hm.put("text", "body");
        hm.put("pubDate", new Date().getTime());
        var maps = new ArrayList<HashMap>();
        maps.add(hm);

        var actual = MessageService.tweetsFromListOfHashMap(maps);

        Assertions.assertEquals(1, actual.size());
        var tweet = actual.get(0);
        Assertions.assertEquals(tweet.getEmail(), "test@test.dk");
        Assertions.assertEquals(tweet.getUsername(), "test");
        Assertions.assertEquals(tweet.getText(), "body");
        Assertions.assertNotNull(tweet.getPubDate());
        Assertions.assertNotNull(tweet.getProfilePic());
    }
}
