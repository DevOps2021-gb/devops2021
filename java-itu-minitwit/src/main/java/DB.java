import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.dieselpoint.norm.Database;

public class DB {
    static Database instance;
    static String DATABASE = "minitwit";
    static String MYSQL = "localhost:3306/";
    static String USER = "root";
    static String PW = "root";

    /*
        Returns a new connection to the database.
    */
    public static Result<Database> connectDb() {
        if (instance == null) {
            try {
                System.setProperty("norm.jdbcUrl", "jdbc:mysql://" + MYSQL + DATABASE + "?useSSL=false");
                System.setProperty("norm.user", USER);
                System.setProperty("norm.password", PW);

                instance = new Database();
            } catch (Exception e) {
                return new Failure<>("could not establish connection to DB");
            }
        }

        return new Success<>(instance);
    }

    public static void setDATABASE(String dbName) {
        DATABASE = dbName;
    }

}
