import Logic.Minitwit;
import Persistence.DB;
import Persistence.Repositories;
import RoP.Result;
import RoP.Success;

import static spark.Spark.stop;

public abstract class DatabaseTestBase {
    int userId = 1;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        DB.setDATABASE("testMinitwit");
        if (System.getProperty("DB_TEST_CONNECTION_STRING") != null) {
            DB.setCONNECTIONSTRING(System.getProperty("DB_TEST_CONNECTION_STRING"));
            DB.setUSER(System.getProperty("DB_USER"));
            DB.setPW(System.getProperty("DB_PASSWORD"));
        }
        Repositories.dropDB();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        stop();
    }

    Result<String> register(String username, String password, String password2, String email){
        if (password2==null) password2 = password;
        if (email==null)     email = username + "@example.com";
        return Minitwit.validateUserCredentialsAndRegister(username, email, password, password2);
    }

    Result<Boolean> login(String username, String password) {
        return Repositories.queryLogin(username, password);
    }

    Result<Boolean> register_and_login(String username, String password) {
        register(username, password, null, null);
        return login(username, password);
    }

    Result<Integer> register_login_getID(String username, String password, String password2, String email) {
        register(username, password, password2, email);
        login(username, password);
        var id = Repositories.getUserId(username);
        assert (id.isSuccess());
        assert (id.get() == userId);
        userId++;
        return id;
    }

    Result<Boolean> logout() {
        //todo logout helper function
        return new Success<>(true);
    }

    void add_message(String text, int loggedInUserId) {
        var rs = Repositories.addMessage(text, loggedInUserId);
        assert (rs.get());
        try {
            Thread.sleep(100);      //hotfix: added to ensure order of messages
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
