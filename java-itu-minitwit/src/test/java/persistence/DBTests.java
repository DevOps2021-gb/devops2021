package persistence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import repository.DB;

class DBTests {


    @Test
    void test_handleArgs_no_or_3_arguments() {
        DB.removeInstance();
        String database = DB.getDatabase(), IP = DB.getIP(), user = DB.getUser(), pw = DB.getPw(), connectionString = DB.getConnectionString();
        Assertions.assertNull(System.getProperty("norm.jdbcUrl"));
        Assertions.assertNull(System.getProperty("norm.user"));
        Assertions.assertNull(System.getProperty("norm.password"));
        var args = new String[]{};

        DB.removeInstance();
        Assertions.assertNull(System.getProperty("norm.jdbcUrl"));
        Assertions.assertNull(System.getProperty("norm.user"));
        Assertions.assertNull(System.getProperty("norm.password"));
        args = new String[]{"abe", "paul", "q123"};
        DB.setDatabaseParameters(args[0], args[1], args[2]);
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
