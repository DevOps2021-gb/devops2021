import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.dieselpoint.norm.Database;

public class DB {
    static final int PORT = 3306;
    static Database instance;
    static String database = "minitwit";
    static String ip = "localhost";
    static String user = "root";
    static String pw = "root";
    static String connectionString;

    private DB() {}

    /*
        Returns a new connection to the database.
    */
    public static Result<Database> connectDb() {
        if (instance == null) {
            try {

                if(connectionString != null) {
                    System.setProperty("norm.jdbcUrl", "jdbc:" + connectionString);
                } else {
                    System.setProperty("norm.jdbcUrl", "jdbc:mysql://" + ip + ":" + PORT + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
                }

                System.setProperty("norm.user", user);
                System.setProperty("norm.password", pw);

                instance = new Database();
            } catch (Exception e) {
                return new Failure<>("could not establish connection to DB");
            }
        }

        return new Success<>(instance);
    }

    public static void setIP(String ip) {
        DB.ip = ip;
    }

    public static void setCONNECTIONSTRING(String connectionString) {
        DB.connectionString = connectionString;
    }

    public static void setUSER(String user) {
        DB.user = user;
    }

    public static void setPW(String pw) {
        DB.pw = pw;
    }

    public static void setDATABASE(String dbName) {
        database = dbName;
    }

}