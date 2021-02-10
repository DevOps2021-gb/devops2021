import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.ibatis.jdbc.ScriptRunner;
import spark.Request;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

            after((request, response) ->  after_request());

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
        get("/",                    (req, res)-> timeline(null, null));
        get("/public",              (req, res)-> public_timeline());
        get("/:username",           (req, res)-> user_timeline());
        get("/:username/follow",    (req, res)-> follow_user(null));
        get("/:username/unfollow",  (req, res)-> unfollow_user(null));
        post("/add_message",        (req, res)-> add_message(null));
        post("/login",              (req, res)-> login("POST", req.params("username"), null, null));
        get("/login",               (req, res)-> login("GET", req.params("username"), null, null));
        get("/register",            (req, res)-> register(null, null, null, null, null));
        post("/register",           (req, res)-> register(null, null, null, null, null));
        get("/logout",              (req, res)-> logout());
    }

    private static Object render_template(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), Charsets.UTF_8), context);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    private static Object render_template(String template) {
        return render_template(template, new HashMap<>());
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
        System.out.println(DATABASE);
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
    static Result<ResultSet> query_db(String query, String... args) {
        try (Connection c = connect_db().get()) {
            PreparedStatement  stmt = c.prepareStatement(query);

            //PreparedStatement indices starts at 1
            for (int i = 1; i < args.length; i++) {
                stmt.setString(i, args[i]);
            }

            ResultSet rs = stmt.executeQuery();

            return new Success<>(rs);
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
            PreparedStatement stmt = c.prepareStatement("select user_id from user where username = ?");
            stmt.setString(1, username);

            var rs = stmt.executeQuery();

            return new Success<>(rs.getInt("user_id"));
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
        var user = query_db("select * from user where user_id = ?", request.params("user_id")).get();

        try {
            int user_id = user.getInt("user_id");
            String username = user.getString("username");
            String email = user.getString("email");
            String pw_hash = user.getString("pw_hash");

            session = new Session(connect_db(), new User(username));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Closes the database again at the end of the request.
     */
    static void after_request() {
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
    static Object timeline(String remote_addr, String user_id) {
        System.out.println("We got a visitor from: " + remote_addr);

        if (session.user == null) {
            return public_timeline();
        }

        try {
            var rs = query_db("""
                    select message.*, user.* from message, user
                    where message.flagged = 0 and message.author_id = user.user_id and (
                      user.user_id = ? or
                      user.user_id in (select whom_id from follower
                           where who_id = ?))
                           order by message.pub_date desc limit ?""", user_id, user_id, PER_PAGE + "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return render_template("timeline.html");
    }

    /*
    Displays the latest messages of all users.
     */
    static Object public_timeline() {

        var rs = query_db("""
                select message.*, user.* from message, user
                        where message.flagged = 0 and message.author_id = user.user_id
                        order by message.pub_date desc limit ?""", PER_PAGE + "");

        return render_template("timeline.html");
    }

    /*
    Display's a users tweets.
     */
    static Object user_timeline() {
        var profile_user = query_db("select * from user where username = ?"); //[username]

        if (profile_user == null) {
            return "404 not found";
        }

        boolean followed = false;
        if (session.user != null) {
            followed = query_db("select 1 from follower where\n" +
                    "            follower.who_id = ? and follower.whom_id = ?") != null; //[session['user_id'], profile_user['user_id']]
        }

        var messages = query_db("select message.*, user.* from message, user where\n" +
                "            user.user_id = message.author_id and user.user_id = ?\n" +
                "            order by message.pub_date desc limit ?"); //[profile_user['user_id'], PER_PAGE]), followed=followed, profile_user=profile_user)
        return render_template("timeline.html");
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object follow_user(String username) {

        if (session.user == null) {
            return "404";
        }

        var whom_id = get_user_id(username);

        if (whom_id == null) {
            return "404";
        }
        try {
            var stmt = session.connection.get().createStatement();

            stmt.executeQuery("insert into follower (who_id, whom_id) values (?, ?)"); //[session['user_id'], whom_id])

            //flash('You are now following "%s"' % username)

        } catch (Exception e) {
            e.printStackTrace();
        }

        //return redirect(url_for('user_timeline', username=username))
        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollow_user(String username) {
        if (session.user == null) {
            return "404";
        }

        var whom_id = get_user_id(username);

        if (whom_id == null) {
            return "404";
        }

        try {
            var stmt = session.connection.get().createStatement();

            stmt.executeQuery("delete from follower where who_id=? and whom_id=?"); //[session['user_id'], whom_id])

            //flash('You are no longer following "%s"' % username)

        } catch (Exception e) {
            e.printStackTrace();
        }

        //return redirect(url_for('user_timeline', username=username))
        return null;
    }

    /*
    Registers a new message for the user.
     */
    static Object add_message(String message) {

        if (session.user == null) {
            return "404";
        }

        if (!message.equals("")) {
            try {
                var stmt = session.connection.get().createStatement();

                stmt.executeQuery("insert into message (author_id, text, pub_date, flagged) values (?, ?, ?, 0)"); //(session['user_id'], request.form['text'], int(time.time())))

                //flash('You are no longer following "%s"' % username)

                //return redirect(url_for('user_timeline', username=username))

            } catch (Exception e) {
                e.printStackTrace();
            }

            //flash('Your message was recorded')
        }

        return timeline(null, null);
    }

    /*
    Logs the user in.
     */
    static Object login(String HTTPVerb, String username, String password1, String password2) {
        if (session.user != null) {
            return timeline(null, null);
        }

        String error = "";
        if (HTTPVerb.equals("POST")) {
            var user = query_db("select * from user where username = ?", username);

            if (!user.isSuccess()) {
                error = "Invalid username";
            } else if (Hashing.check_password_hash(password1, password2)) {
                error = "Invalid password";
            } else {
                //flash('You were logged in')
                session.user = new User("");
                return timeline(null, null);
            }
        }
        if(error.length()>0) System.out.println(error);

        String finalError = error; //must be effectively final
        return render_template("login.html", new HashMap<>() {{
            put("error", finalError);
        }});
    }

    /*
    Registers the user.
     */
    static Object register(String HTTPVerb, String username, String email, String password, String password2) {
        if (session.user != null) {
            return timeline(null, null);
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
                    PreparedStatement stmt = session.connection.get().prepareStatement("insert into user (username, email, pw_hash) values (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, Hashing.generate_password_hash(password));

                    stmt.executeUpdate();

                    //flash('You were successfully registered and can login now')
                    return login(null, null, null, null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(error.length() > 0) System.out.println(error);

        String finalError = error; //must be effectively final
        return render_template("register.html", new HashMap<>() {{
            put("error", finalError);
        }});
    }

    /*
    Logs the user out
     */
    static Object logout() {
        //flash('You were logged out')
        session.user = null;
        return public_timeline();
    }

    public static void setDATABASE(String database){
        minitwit.DATABASE = database;
    }

}
