import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(true, id1.get().equals(id1Rs.get()));
        Assertions.assertEquals(true,  user1.get().id == id1.get());
        Assertions.assertEquals("foo",  user1.get().getUsername());
        Assertions.assertEquals(Hashing.generatePasswordHash("default").get(),  user1.get().getPwHash());
        Assertions.assertEquals("myEmail@itu.dk",  user1.get().getEmail());
        Assertions.assertEquals(user1.get().id,             user12.get().id);
        Assertions.assertEquals(user1.get().getUsername(),  user12.get().getUsername());
        Assertions.assertEquals(user1.get().getPwHash(),    user12.get().getPwHash());
        Assertions.assertEquals(user1.get().getEmail(),     user12.get().getEmail());
    }
}
