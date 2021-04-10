package persistence;

import org.junit.jupiter.api.Assertions;
import testUtilities.DatabaseTestBase;
import services.MaintenanceService;
import org.junit.jupiter.api.Test;
import utilities.Hashing;

class MessageRepositoryTests extends DatabaseTestBase {
    @Test
    void testGetTweetsByUsername() {
        var id1 = this.registerLoginGetID("foo", "default",  null);
        this.addMessage("the message by foo", id1.get());
        this.logout();
        var id2 = this.registerLoginGetID("bar","default",  null);
        this.addMessage("the message by bar", id2.get());
        var rs = MessageRepository.getTweetsByUsername("foo");
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet1 = rs.get().get(0);
        Assertions.assertEquals("foo@example.com", tweet1.getEmail());
        Assertions.assertEquals("foo", tweet1.getUsername());
        Assertions.assertEquals("the message by foo", tweet1.getText());
        Assertions.assertEquals(Hashing.getGravatarUrl(tweet1.getEmail()), tweet1.getProfilePic());
        rs = MessageRepository.getTweetsByUsername("bar");
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet2 = rs.get().get(0);
        Assertions.assertEquals("bar@example.com", tweet2.getEmail());
        Assertions.assertEquals("bar", tweet2.getUsername());
        Assertions.assertEquals("the message by bar", tweet2.getText());
        Assertions.assertEquals(Hashing.getGravatarUrl(tweet2.getEmail()), tweet2.getProfilePic());
    }

    @Test
    void testGetPersonalTweetsById() {
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 0);
        MaintenanceService.processMessages();
        Assertions.assertEquals(true, (int) MaintenanceService.getMessages() == 0);
        var id1 = this.registerLoginGetID("foo", "default",  null);
        this.addMessage("the message by foo", id1.get());
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 1);
        MaintenanceService.processMessages();
        Assertions.assertEquals(true, MaintenanceService.getMessages() == 1);
        this.logout();
        var id2 = this.registerLoginGetID("bar","default",  null);
        this.addMessage("the message by bar", id2.get());
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 2);
        MaintenanceService.processMessages();
        Assertions.assertEquals(true, (int) MaintenanceService.getMessages() == 2);
        var rs = MessageRepository.getPersonalTweetsById(id1.get());
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet1 = rs.get().get(0);
        Assertions.assertEquals("foo@example.com", tweet1.getEmail());
        Assertions.assertEquals("foo", tweet1.getUsername());
        Assertions.assertEquals("the message by foo", tweet1.getText());
        rs = MessageRepository.getPersonalTweetsById(id2.get());
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet2 = rs.get().get(0);
        Assertions.assertEquals("bar@example.com", tweet2.getEmail());
        Assertions.assertEquals("bar", tweet2.getUsername());
        Assertions.assertEquals("the message by bar", tweet2.getText());
    }
}
