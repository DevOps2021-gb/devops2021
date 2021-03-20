
import persistence.UserRepository;
import utilities.Hashing;
import org.junit.jupiter.api.Test;

class UserTests extends DatabaseTestBase {
    @Test
    void testQueryGetUser() {
        var id1 = this.registerLoginGetID("foo", "default",  "myEmail@itu.dk");
        var user1 = UserRepository.getUser("foo");
        var id1Rs = UserRepository.getUserId("foo");
        var user12 = UserRepository.getUserById(id1.get());
        assert id1.get().equals(id1Rs.get());
        assert user1.get().id == id1.get();
        assert user1.get().getUsername().equals("foo");
        assert user1.get().getPwHash().equals(Hashing.generatePasswordHash("default").get());
        assert user1.get().getEmail().equals("myEmail@itu.dk");
        assert
            user1.get().id == user12.get().id
                && user1.get().getUsername().equals(user12.get().getUsername())
                && user1.get().getPwHash().equals(user12.get().getPwHash())
                && user1.get().getEmail().equals(user12.get().getEmail());
    }
}
