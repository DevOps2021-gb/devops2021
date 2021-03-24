
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.DB;

class MainDBUsageTests {
    @Test
    void test_handleArgs_no_arguments() {
        System.out.println("start 0");
        String database = DB.getDatabase(), IP = DB.getIP(), user = DB.getUser(), pw = DB.getPw(), connectionString = DB.getConnectionString();
        var args = new String[]{};
        Main.handleArgs(args);
        Assertions.assertEquals(database,           DB.getDatabase());
        Assertions.assertEquals(IP,                 DB.getIP());
        Assertions.assertEquals(user,               DB.getUser());
        Assertions.assertEquals(pw,                 DB.getPw());
        Assertions.assertEquals(connectionString,   DB.getConnectionString());
        System.out.println("end 0");
    }

    @Test
    void test_handleArgs_3_arguments() {
        System.out.println("start 1");
        String database = DB.getDatabase(), IP = DB.getIP(), user = DB.getUser(), pw = DB.getPw(), connectionString = DB.getConnectionString();
        var args = new String[]{"abe", "paul", "q123"};
        Main.handleArgs(args);
        Assertions.assertEquals(database,           DB.getDatabase());
        Assertions.assertEquals(IP,                 DB.getIP());
        Assertions.assertEquals("paul",    DB.getUser());
        Assertions.assertEquals("q123",    DB.getPw());
        Assertions.assertEquals("abe",     DB.getConnectionString());
        Assertions.assertNotEquals(user, "paul");
        Assertions.assertNotEquals(pw, "q123");
        Assertions.assertNotEquals(connectionString,"abe");
        System.out.println("end 1");
    }


}
