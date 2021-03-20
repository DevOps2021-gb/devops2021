import Persistence.UserRepository;
import Utilities.Hashing;
import org.junit.jupiter.api.Test;

public class UserTests extends DatabaseTestBase {
    @Test
    void test_queryGetUser() {
        var id1 = register_login_getID("foo", "default",  "myEmail@itu.dk");
        var user1_1 = UserRepository.getUser("foo");
        var id1_rs = UserRepository.getUserId("foo");
        var user1_2 = UserRepository.getUserById(id1.get());

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
}
