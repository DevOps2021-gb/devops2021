import Logic.Logger;
import Persistence.MessageRepository;
import org.junit.jupiter.api.Test;

public class MessageTests extends DatabaseTestBase {
    @Test
    void test_getTweetsByUsername() {
        var id1 = register_login_getID("foo", "default",  null);
        add_message("the message by foo", id1.get());
        logout();
        var id2 = register_login_getID("bar","default",  null);
        add_message("the message by bar", id2.get());

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
    void test_getPersonalTweetsById() {
        assert (MessageRepository.countMessages().get() == 0);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 0);

        var id1 = register_login_getID("foo", "default",  null);
        add_message("the message by foo", id1.get());
        assert (MessageRepository.countMessages().get() == 1);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 1);


        logout();
        var id2 = register_login_getID("bar","default",  null);
        add_message("the message by bar", id2.get());
        assert (MessageRepository.countMessages().get() == 2);
        Logger.processMessages();
        assert ((int) Logger.getMessages() == 2);

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
