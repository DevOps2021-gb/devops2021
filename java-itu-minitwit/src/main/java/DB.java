import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.dieselpoint.norm.Database;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DB {
    private static SessionFactory dbConnectionFactory;
    private static Session instance;
    static String DATABASE = "minitwit";    //todo: use
    static String IP = "minitwit_mysql"; //docker container name    //todo: replace in hibernate.cfg.xml

    /*
        Returns a new connection to the database.
    */
    public static Result<Session> connectDb() {
        if (instance == null || !instance.isOpen()) {
            try {
                dbConnectionFactory = new Configuration().configure().buildSessionFactory();
                instance = dbConnectionFactory.openSession();
            } catch (Exception e) {
                return new Failure<>("could not establish connection to DB");
            }
        }
        return new Success<>(instance);
    }

    public static void setIP(String IP) {
        System.out.println("Database IP set to: " + IP);
        DB.IP = IP;
    }

    public static void setDATABASE(String dbName) {
        DATABASE = dbName;
    }

}
