package testUtilities;

import org.junit.jupiter.api.Assertions;
import repository.DB;
import repository.MessageRepository;
import repository.UserRepository;
import errorhandling.Result;
import errorhandling.Success;
import org.junit.jupiter.api.BeforeEach;

import static spark.Spark.stop;

public abstract class DatabaseTestBase {
    int userId = 1;

    @BeforeEach
    void setUp() {
        DB.removeInstance();
        DB.setDatabase("testMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            DB.setDatabaseParameters(System.getProperty("DB_TEST_CONNECTION_STRING"), System.getProperty("DB_USER"), System.getProperty("DB_PASSWORD"));
        }
        DB.dropDatabase();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        stop();
    }

    public Result<String> register(String username, String password, String email) {
        if (email == null) {
            email = username + "@example.com";
        }
        return UserRepository.addUser(username, email, password);
    }

    public Result<Boolean> login(String username, String password) {
        return UserRepository.queryLogin(username, password);
    }

    public Result<Boolean> registerAndLogin(String username, String password) {
        this.register(username, password, null);
        return this.login(username, password);
    }

    public Result<Integer> registerLoginGetID(String username, String password, String email) {
        this.register(username, password, email);
        this.login(username, password);
        var id = UserRepository.getUserId(username);
        Assertions.assertTrue(id.isSuccess());
        Assertions.assertEquals(this.userId, (int) id.get());
        this.userId++;
        return id;
    }

    public Result<Boolean> register_and_login(String username, String password) {
        register(username, password, null);
        return login(username, password);
    }

    public  Result<Integer> register_login_getID(String username, String password, String email) {
        register(username, password, email);
        login(username, password);
        var id = UserRepository.getUserId(username);
        Assertions.assertTrue(id.isSuccess());
        Assertions.assertEquals(userId, (int) id.get());
        userId++;
        return id;
    }

    public Result<Boolean> logout() {
        //todo logout helper function
        return new Success<>(true);
    }

    //hotfix: added Thread.sleep to ensure order of messages
    public void addMessage(String text, int loggedInUserId) {
        var rs = MessageRepository.addMessage(text, loggedInUserId);
        Assertions.assertEquals(true, rs.get());
    }
}
