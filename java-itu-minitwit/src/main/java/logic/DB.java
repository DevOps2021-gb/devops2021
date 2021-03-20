package logic;

import rop.Failure;
import rop.Result;
import rop.Success;
import com.dieselpoint.norm.Database;

public final class DB {
    static final int PORT = 3306;
    private static Database instance;
    private static String database = "Logic.minitwit";
    private static String ip = "localhost";
    private static String user = "root";
    private static String pw = user;
    private static String connectionString;

    private DB() {}

    /*
        Returns a new connection to the database.
    */
    public static Result<Database> connectDb() {
        Result<Database> result = new Success<>(instance);
        if (instance == null) {
            try {
                String normjdbc = "norm.jdbcUrl";

                if(connectionString == null) {
                    System.setProperty(normjdbc, "jdbc:mysql://" + ip + ":" + PORT + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
                } else {
                    System.setProperty(normjdbc, "jdbc:" + connectionString);
                }

                System.setProperty("norm.user", user);
                System.setProperty("norm.password", pw);

                instance = new Database();
                result = new Success<>(instance);
            } catch (Exception e) {
                result = new Failure<>("could not establish connection to Logic.DB");
            }
        }

        return result;
    }

    public static void setIP(final String ip) {
        DB.ip = ip;
    }

    public static void setCONNECTIONSTRING(final String connectionString) {
        DB.connectionString = connectionString;
    }

    public static void setUSER(final String user) {
        DB.user = user;
    }

    public static void setPW(final String pw) {
        DB.pw = pw;
    }

    public static void setDATABASE(final String dbName) {
        database = dbName;
    }

}