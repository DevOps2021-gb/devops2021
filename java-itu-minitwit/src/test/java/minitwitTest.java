import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class minitwitTest {
    File databaseFile;
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        try {
            databaseFile = File.createTempFile("testDB-", ".db");
            minitwit.setDATABASE(databaseFile.getName());
            minitwit.init_db();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        databaseFile.delete();
    }
}