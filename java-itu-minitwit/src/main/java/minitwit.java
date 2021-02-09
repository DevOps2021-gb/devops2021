import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.loader.ClasspathResourceLocator;
import org.apache.ibatis.jdbc.ScriptRunner;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.swing.plaf.nimbus.State;
import java.io.FileReader;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import static spark.Spark.*;

public class minitwit {

    //configuration
    static String DATABASE      = "minitwit.db";
    static int PER_PAGE         = 30;
    static Boolean DEBUG        = true;
    static String SECRET_KEY    = "development key";
    static Session session; //TODO handle multiple sessions?

    public static void main(String[] args) {
        try {
            before((request, response) -> before_request(request));

            after((request, response) ->  after_request(response));

            notFound((req, res) -> {
                res.type("application/json");
                return "{\"message\":\"Custom 404\"}";
            });

            internalServerError((req, res) -> {
                res.type("application/json");
                return "{\"message\":\"Custom 500 handling\"}";
            });

            registerEndpoints();

            init_db();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerEndpoints() {
        get("/",                    (req, res)-> timeline());
        get("/public",              (req, res)-> public_timeline());
        get("/:username",           (req, res)-> user_timeline());
        get("/:username/follow",    (req, res)-> follow_user());
        get("/:username/unfollow",  (req, res)-> unfollow_user());
        post("/add_message",        (req, res)-> add_message());
        post("/login",              (req, res)-> login("POST", req.params("username")));
        get("/login",               (req, res)-> login("GET", req.params("username")));
        get("/register",            (req, res)-> register(null, null, null, null, null));
        post("/register",           (req, res)-> register(null, null, null, null, null));
        get("/logout",              (req, res)-> logout());
    }

    /*
    Returns a new connection to the database.
     */
    private static Result<Connection> connect_db() {
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Connected to " + meta.getDriverName());
                return new Success<>(conn);
            }

            return new Failure<>("could not establish connection to DB");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return new Failure<>(e);
        }
    }

    /*
    Creates the database tables.
     */
    public static void init_db()  {
        try (Connection c = connect_db().get()) {

            ScriptRunner sr = new ScriptRunner(c);

            sr.runScript(new FileReader("schema.sql"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Queries the database and returns a list of dictionaries.
     */
    static Result<List<Map<Integer, Object>>> query_db(String query, String... args) {
        try (Connection c = connect_db().get()) {
            PreparedStatement  stmt = c.prepareStatement(query);

            for (int i = 1; i < args.length; i++) {
                stmt.setString(i, args[i]);
            }

            ResultSet rs = stmt.executeQuery();

            List<Map<Integer, Object>> queryResult = new ArrayList<>();
            int idx = 0;
            while (rs.next()) {
                HashMap<Integer, Object> hm = new HashMap<>();
                hm.put(idx, rs.getObject(idx));
                queryResult.add(hm);
            }

            return new Success<>(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
    Convenience method to look up the id for a username.
     */
    static Result<Integer> get_user_id(String username) {
        try (Connection c = connect_db().get()) {
            Statement stmt = c.createStatement();

            //vulnerable to SQL injection? fix?
            var rs = stmt.executeQuery("select user_id from user where username = " + username);

            return new Success<>(rs.getInt(0));
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
    Format a timestamp for display.
     */
    Result<String> format_datetime(String timestamp) {
        try {
            //https://stackoverflow.com/questions/18915075/java-convert-string-to-timestamp
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(timestamp);
            return new Success<>(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
    Return the gravatar image for the given email address.
     */
    void gravatar_url(String email, int size) {
        //hashing boogaloo
        //return 'http://www.gravatar.com/avatar/%s?d=identicon&s=%d' % \ (md5(email.strip().lower().encode('utf-8')).hexdigest(), size)
    }
    /*
    Java does not support default arguments
     */
    void gravatar_url(String email) {
        gravatar_url(email, 80);
    }

    /*
    Make sure we are connected to the database each request and look
    up the current user so that we know he's there.
     */
    static void before_request(Request request) {
        var user = query_db("select * from user where user_id = ?", "user_id").get();
        session = new Session(connect_db(), new User(0));
    }

    /*
    Closes the database again at the end of the request.
     */
    static void after_request(Response response) {
        try {
            session.connection.get().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline() {
        return null;
    }

    /*
    Displays the latest messages of all users.
     */
    static Object public_timeline() {
        return null;
    }

    /*
    Display's a users tweets.
     */
    static Object user_timeline() {
        return null;
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object follow_user() {

        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollow_user() {
        return null;
    }

    /*
    Registers a new message for the user.
     */
    static Object add_message() {
        return null;
    }

    /*
    Logs the user in.
     */
    static Object login(String HTTPVerb, String username) {
        if (session.user != null) {
            return timeline();
        }

        String error = "";
        if (HTTPVerb.equals("POST")) {
            var user = query_db("select * from user where username = ?", username);

            if (!user.isSuccess()) {
                error = "Invalid username";
            } else if (false) /* todo: CHECK PASSWORD HASH */{
                error = "Invalid password";
            } else {
                //flash('You were logged in')
                session.user = new User(0);
                //return redirect(url_for('timeline'))
            }
        }
        if(error.length()>0) System.out.println(error);

        //return render_template('login.html', error=error)
        return null;
    }

    /*
    Registers the user.
     */
    static Object register(String HTTPVerb, String username, String email, String password, String password2) {
        if (session.user != null) {
            return timeline();
        }

        String error = "";
        if (HTTPVerb.equals("POST")) {
            if (username == null) {
                error = "You have to enter a username";
            } else if (email == null || !email.contains("@")) {
                error = "You have to enter a valid email address";
            } else if (password == null) {
                error = "You have to enter a password";
            } else if (!password.equals(password2)) {
                error = "The two passwords do not match";
            } else if (get_user_id(username).isSuccess()) {
                error = "The username is already taken";
            } else {
                try {
                    Statement stmt = session.connection.get().createStatement();

                    var rs = stmt.executeQuery("insert into user (username, email, pw_hash) values (?, ?, ?)");
                    //[request.form['username'], request.form['email'],generate_password_hash(request.form['password'])]

                    //flash('You were successfully registered and can login now')
                    return login(null, null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(error.length()>0) System.out.println(error);

        //return render_template('register.html', error=error)
        return null;
    }

    /*
    Logs the user out
     */
    static Object logout() {
        //flash('You were logged out')
        session.user = null;
        return public_timeline();
    }
}
