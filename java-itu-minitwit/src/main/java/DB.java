import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.dieselpoint.norm.Database;

public class DB {
    static Database instance;
    static String DATABASE = "minitwit";
    static String IP = "localhost";
    static int PORT = 3306;
    static String USER = "root";
    static String PW = "root";
    static String CONNECTIONSTRING;

    /*
        Returns a new connection to the database.
    */
    public static Result<Database> connectDb() {
        if (instance == null) {
            try {

                if(CONNECTIONSTRING != null) {
                    System.setProperty("norm.jdbcUrl", "jdbc:" + CONNECTIONSTRING);
                } else {
                    System.setProperty("norm.jdbcUrl", "jdbc:mysql://" + IP + ":" + PORT + "/" + DATABASE + "?allowPublicKeyRetrieval=true&useSSL=false");
                }

                System.setProperty("norm.user", USER);
                System.setProperty("norm.password", PW);

                instance = new Database();
            } catch (Exception e) {
                return new Failure<>("could not establish connection to DB");
            }
        }

        return new Success<>(instance);
    }

    public static void setIP(String IP) {
        DB.IP = IP;
    }

    public static void setCONNECTIONSTRING(String CONNECTIONSTRING) {
        DB.CONNECTIONSTRING = CONNECTIONSTRING;
    }

    public static void setUSER(String USER) {
        DB.USER = USER;
    }

    public static void setPW(String PW) {
        DB.PW = PW;
    }

    public static void setDATABASE(String dbName) {
        DATABASE = dbName;
    }

}