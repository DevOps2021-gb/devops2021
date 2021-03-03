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
    static String IP = "minitwit_mysql"; //docker container name    //todo: replace in hibernate.cfg.xml or below

    /*
        Returns a new connection to the database.
    */
    public static Result<Session> connectDb() {
        try {
            if(dbConnectionFactory == null){
                var config = new Configuration().setProperty("connection.url", "jdbc:mysql://localhost:3306/"+DATABASE+"?allowPublicKeyRetrieval=true&amp;useSSL=false");
                dbConnectionFactory = config.configure().buildSessionFactory();
            }
            if (instance == null || !instance.isOpen()) {
                instance = dbConnectionFactory.openSession();
            }
        } catch (Exception e) {
            return new Failure<>("could not establish connection to DB");
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
    public static Result<Session>  dropDATABASE(){
        instance.beginTransaction();
        instance.createSQLQuery("drop table if exists Follower").executeUpdate();
        instance.createSQLQuery("drop table if exists Message").executeUpdate();
        instance.createSQLQuery("drop table if exists User").executeUpdate();
        instance.getTransaction().commit();
        instance.close();
        instance = null;
        dbConnectionFactory = null;
        return connectDb();
    }

}
