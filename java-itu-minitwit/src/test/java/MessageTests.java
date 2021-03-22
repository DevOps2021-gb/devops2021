import org.junit.jupiter.api.Assertions;
import services.LogService;
import persistence.MessageRepository;
import org.junit.jupiter.api.Test;

class MessageTests extends DatabaseTestBase {
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
        rs = MessageRepository.getTweetsByUsername("bar");
        Assertions.assertEquals(true, rs.isSuccess());
        var tweet2 = rs.get().get(0);
        Assertions.assertEquals("bar@example.com", tweet2.getEmail());
        Assertions.assertEquals("bar", tweet2.getUsername());
        Assertions.assertEquals("the message by bar", tweet2.getText());
    }

    @Test
    void testGetPersonalTweetsById() {
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 0);
        LogService.processMessages();
        Assertions.assertEquals(true, (int) LogService.getMessages() == 0);
        var id1 = this.registerLoginGetID("foo", "default",  null);
        this.addMessage("the message by foo", id1.get());
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 1);
        LogService.processMessages();
        Assertions.assertEquals(true, LogService.getMessages() == 1);
        this.logout();
        var id2 = this.registerLoginGetID("bar","default",  null);
        this.addMessage("the message by bar", id2.get());
        Assertions.assertEquals(true, MessageRepository.countMessages().get() == 2);
        LogService.processMessages();
        Assertions.assertEquals(true, (int) LogService.getMessages() == 2);
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
