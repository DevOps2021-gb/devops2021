
import services.LogService;
import persistence.MessageRepository;
import org.junit.jupiter.api.Test;

class MessageTests extends DatabaseTestBase {
    @Test
    void testGetTweetsByUsername() {
        var id1 = this.registerLoginGetID("foo", "default",  null);
        addMessage("the message by foo", id1.get());
        logout();
        var id2 = this.registerLoginGetID("bar","default",  null);
        addMessage("the message by bar", id2.get());
        var rs = MessageRepository.getTweetsByUsername("foo");
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.getEmail().equals("foo@example.com"));
        assert (tweet1.getUsername().equals("foo"));
        assert (tweet1.getText().equals("the message by foo"));
        rs = MessageRepository.getTweetsByUsername("bar");
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.getEmail().equals("bar@example.com"));
        assert (tweet2.getUsername().equals("bar"));
        assert (tweet2.getText().equals("the message by bar"));
    }

    @Test
    void testGetPersonalTweetsById() {
        assert (MessageRepository.countMessages().get() == 0);
        LogService.processMessages();
        assert ((int) LogService.getMessages() == 0);
        var id1 = this.registerLoginGetID("foo", "default",  null);
        this.addMessage("the message by foo", id1.get());
        assert (MessageRepository.countMessages().get() == 1);
        LogService.processMessages();
        assert ((int) LogService.getMessages() == 1);
        logout();
        var id2 = this.registerLoginGetID("bar","default",  null);
        this.addMessage("the message by bar", id2.get());
        assert (MessageRepository.countMessages().get() == 2);
        LogService.processMessages();
        assert ((int) LogService.getMessages() == 2);
        var rs = MessageRepository.getPersonalTweetsById(id1.get());
        assert (rs.isSuccess());
        var tweet1 = rs.get().get(0);
        assert (tweet1.getEmail().equals("foo@example.com"));
        assert (tweet1.getUsername().equals("foo"));
        assert (tweet1.getText()).equals("the message by foo");
        rs = MessageRepository.getPersonalTweetsById(id2.get());
        assert (rs.isSuccess());
        var tweet2 = rs.get().get(0);
        assert (tweet2.getEmail().equals("bar@example.com"));
        assert (tweet2.getUsername().equals("bar"));
        assert (tweet2.getText().equals("the message by bar"));
    }
}
