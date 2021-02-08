import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import java.sql.*;
import java.util.Scanner;
import java.util.Map;

import static spark.Spark.*;

public class minitwit {

    //configuration
    static String DATABASE = "minitwit.db";
    static int PER_PAGE = 30;
    static Boolean DEBUG = true;
    static String SECRET_KEY = "development key";

    public static void main(String[] args) throws Exception {
        try {
            after((request, response) -> {
                after_request();
            });

            before((request, response) -> {
                before_request();
            });

            init_db();

            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Jinjava jinjava = new Jinjava();
//        Map<String, Object> context = Maps.newHashMap();
//        context.put("name", "Jared");
//
//        String template = Resources.toString(Resources.getResource("my-template.html"), Charsets.UTF_8);
//
//        String renderedTemplate = jinjava.render(template, context);
//
//        get("/hello", (req, res) -> renderedTemplate);
    }

    /*
    Returns a new connection to the database.
     */
    private static Connection connect_db() {
        String url = "jdbc:sqlite:" + DATABASE;

        try{
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Connected to " + meta.getDriverName());
                return conn;
            }

            return null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    /*
    Creates the database tables.
     */
    public static void init_db() throws Exception {
        Connection c = null;

        try {
            c = connect_db();

            if (c.isClosed()) throw new Exception("Closed!");


            String sql1 = "drop table if exists user;";
            String sql2 = "create table user (\n" +
                    "  user_id integer primary key autoincrement,\n" +
                    "  username string not null,\n" +
                    "  email string not null,\n" +
                    "  pw_hash string not null\n" +
                    ");";

            String sql3 = "drop table if exists follower;";
            String sql4 = "create table follower (\n" +
                    "  who_id integer,\n" +
                    "  whom_id integer\n" +
                    ");";

            String sql5 = "drop table if exists message;";
            String sql6 = "create table message (\n" +
                    "  message_id integer primary key autoincrement,\n" +
                    "  author_id integer not null,\n" +
                    "  text string not null,\n" +
                    "  pub_date integer,\n" +
                    "  flagged integer\n" +
                    ");";

            Statement stmt = c.createStatement();
            //String[] sqls = new String{sql1,sql2,sql3,sql4,sql5,sql6}
            // for(String sql : sqls) {stmt.execute(sql);}
            // create a new table
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            stmt.execute(sql4);
            stmt.execute(sql5);
            stmt.execute(sql6);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
    }

    /*
    Queries the database and returns a list of dictionaries.
     */
    void query_db() {

    }

    /*
    Convenience method to look up the id for a username.
     */
    void get_user_id() {

    }

    /*
    Format a timestamp for display.
     */
    void format_datetime() {


    }

    /*
    Return the gravatar image for the given email address.
     */
    void gravatar_url() {

    }

    /*
    Make sure we are connected to the database each request and look
    up the current user so that we know he's there.
     */
    static void before_request() {

    }

    /*
    Closes the database again at the end of the request.
     */
    static void after_request() {

    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    void timeline() {

    }

    /*
    Displays the latest messages of all users.
     */
    void public_timeline() {

    }

    /*
    Display's a users tweets.
     */
    void user_timeline() {

    }

    /*
    Adds the current user as follower of the given user.
     */
    void follow_user() {


    }

    /*
    Removes the current user as follower of the given user.
     */
    void unfollow_user() {

    }

    /*
    Registers a new message for the user.
     */
    void add_message() {

    }

    /*
    Logs the user in.
     */
    void login() {

    }

    /*
    Registers the user.
     */
    void register() {

    }

    /*
    Logs the user out
     */
    void logout() {

    }
}
