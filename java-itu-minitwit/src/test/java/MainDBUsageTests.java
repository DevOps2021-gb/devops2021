
import main.Main;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import persistence.DB;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MainDBUsageTests {
    @Test
    void test_handleArgs_no_or_3_arguments() {
        DB.removeInstance();
        String database = DB.getDatabase(), IP = DB.getIP(), user = DB.getUser(), pw = DB.getPw(), connectionString = DB.getConnectionString();
        Assertions.assertEquals(null,         System.getProperty("norm.jdbcUrl"));
        Assertions.assertEquals(null,         System.getProperty("norm.user"));
        Assertions.assertEquals(null,         System.getProperty("norm.password"));
        var args = new String[]{};
        Main.handleArgs(args);
        DB.connectDb();
        Assertions.assertEquals(database,           DB.getDatabase());
        Assertions.assertEquals(IP,                 DB.getIP());
        Assertions.assertEquals(user,               DB.getUser());
        Assertions.assertEquals(pw,                 DB.getPw());
        Assertions.assertEquals(connectionString,   DB.getConnectionString());
        Assertions.assertEquals(true, System.getProperties().containsKey("norm.jdbcUrl"));
        Assertions.assertEquals(user,               System.getProperty("norm.user"));
        Assertions.assertEquals(pw,                 System.getProperty("norm.password"));

        DB.removeInstance();
        Assertions.assertEquals(null,         System.getProperty("norm.jdbcUrl"));
        Assertions.assertEquals(null,         System.getProperty("norm.user"));
        Assertions.assertEquals(null,         System.getProperty("norm.password"));
        args = new String[]{"abe", "paul", "q123"};
        Main.handleArgs(args);
        DB.connectDb();
        Assertions.assertEquals(database,           DB.getDatabase());
        Assertions.assertEquals(IP,                 DB.getIP());
        Assertions.assertEquals("paul",    DB.getUser());
        Assertions.assertEquals("q123",    DB.getPw());
        Assertions.assertEquals("abe",     DB.getConnectionString());
        Assertions.assertEquals("jdbc:" + DB.getConnectionString(),         System.getProperty("norm.jdbcUrl"));
        Assertions.assertEquals("paul",             System.getProperty("norm.user"));
        Assertions.assertEquals("q123",             System.getProperty("norm.password"));
    }


}
