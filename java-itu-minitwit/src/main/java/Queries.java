import RoP.Failure;
import RoP.Result;
import RoP.Success;
import org.apache.ibatis.jdbc.ScriptRunner;
import spark.Request;

import java.io.FileReader;
import java.sql.*;

public class Queries {

    static String DATABASE      = "minitwit.db";
    static int PER_PAGE         = 30;
    static Session session; //TODO handle multiple sessions?

    /*
    Returns a new connection to the database.
     */
    public static Result<Connection> connect_db() {
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
        try {
            Connection c = connect_db().get();
            ScriptRunner sr = new ScriptRunner(c);

            sr.runScript(new FileReader("schema.sql"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    Queries the database and returns a list of dictionaries.
     */
    static Result<ResultSet> query_db_select(String query, String... args) {
        try{
            Connection c = connect_db().get();
            PreparedStatement  stmt = c.prepareStatement(query);
            //PreparedStatement indices starts at 1
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i+1, args[i]);
            }
            ResultSet rs = stmt.executeQuery();

            return new Success<>(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }
    static Integer query_db_update(String query, String... args) {
        try{
            Connection c = connect_db().get();
            PreparedStatement  stmt = c.prepareStatement(query);
            //PreparedStatement indices starts at 1
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i+1, args[i]);
            }
            int res = stmt.executeUpdate();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Make sure we are connected to the database each request and look
    up the current user so that we know he's there.
     */

    public static void before_request(Request request) {
        var user = query_db_select("select * from user where user_id = ?", request.params("user_id")).get();

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
    public static void after_request() {
        try {
            session.connection.get().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet get_user(String username) throws SQLException {
        var rs = query_db_select("select user_id from user where username = ?", username);
        return rs.get();
    }

    public static Boolean user_logged_in(){
        return session != null && session.user != null;
    }

    public static Result<ResultSet> following(ResultSet profile_user) throws SQLException {     //todo fix
        return query_db_select("""
                                    select 1 from follower where
                                    follower.who_id = ? and follower.whom_id = ?""");//, [[session["user_id"]; profile_user["user_id"]]]);

    }

    public static Result<ResultSet> messages(ResultSet profile_user) throws SQLException {     //todo fix
        return query_db_select("""
                select message.*, user.* from message, user where
                    user.user_id = message.author_id and user.user_id = ?
                    order by message.pub_date desc limit ?"""); //[profile_user['user_id'], PER_PAGE]), followed=followed, profile_user=profile_user)
    }

    /*
    Convenience method to look up the id for a username.
     */
    public static Result<Integer> get_user_id(String username) {
        try{
            var rs = get_user(username);
            if(rs.isClosed()) return new Failure<>(new Exception("User doesn't exist"));
            return new Success<>(rs.getInt("user_id"));
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
    Current user follow username
    */
    static Object follow_user(String username) {

        if (!user_logged_in()) {  return null; }

        var whom_id = get_user_id(username);
        if (whom_id == null) { return null; }

        try {
            var stmt = session.connection.get().createStatement();

            return stmt.executeQuery("insert into follower (who_id, whom_id) values (?, ?)"); //todo: [session['user_id'], whom_id]
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
    Current user unfollow user
    */
    static Object unfollow_user(String username) {

        if(!user_logged_in()){ return null; }

        var whom_id = get_user_id(username);
        if(whom_id == null){ return null; }

        try{
            var stmt = session.connection.get().createStatement();

            return stmt.executeQuery("delete from follower where (who_id, whom_id) values (?, ?) limit 1"); // todo: [session['user_id'], whom_id]
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     Displays the latest messages of all users.
    */
    public static Result<ResultSet> public_timeline() {
        var rs = query_db_select("""
            select message.*, user.* from message, user
                where message.flagged = 0 and message.author_id = user.user_id
                order by message.pub_date desc limit ?""", PER_PAGE + "");
        return rs;
        //return render_template("timeline.html");
    }

    public static Result<ResultSet> timeline(String user_id) {
        try {
            var rs = Queries.query_db_select("""
                    select message.*, user.* from message, user
                    where message.flagged = 0 and message.author_id = user.user_id and (
                      user.user_id = ? or
                      user.user_id in (select whom_id from follower
                           where who_id = ?))
                           order by message.pub_date desc limit ?""", user_id, user_id, PER_PAGE + "");
            return rs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
        Registers a new message for the user.
    */
    public static Integer add_message(String message) {
        if (!user_logged_in()) { return null;}

        if (!message.equals("")) {
            try {
                var rs = query_db_update("insert into message (author_id, text, pub_date, flagged) values (?, ?, ?, 0)",
                        "0", message, "0");//todo: (session['user_id'], request.form['text'], int(time.time())))
                return rs;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static String login(String HTTPVerb, String username, String password1, String password2) {
        String error = "";
        if (HTTPVerb.equals("POST")) {
            var user = query_db_select("select * from user where username = ?", username);

            if (!user.isSuccess()) {
                error = "Invalid username";
            } else if (!Hashing.check_password_hash(password1, password2)) {
                error = "Invalid password";
            } else {
                //flash('You were logged in')
                session.user = new User("");
            }
        }
        return error;
    }

    static String register(String HTTPVerb, String username, String email, String password1, String password2) {
        String error = "";
        if (HTTPVerb.equals("POST")) {
            if (username == null || username == "") {
                error = "You have to enter a username";
            } else if (email == null || !email.contains("@")) {
                error = "You have to enter a valid email address";
            } else if (password1 == null || password1 == "") {
                error = "You have to enter a password";
            } else if (!password1.equals(password2)) {
                error = "The two passwords do not match";
            } else if (get_user_id(username).isSuccess()) {
                error = "The username is already taken";
            } else {
                try {
                    PreparedStatement stmt = session.connection.get().prepareStatement("insert into user (username, email, pw_hash) values (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, Hashing.generate_password_hash(password1));

                    stmt.executeUpdate();

                    //flash('You were successfully registered and can login now')
                    return login(HTTPVerb, username, password1, password2);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return error;
    }



    public static void setDATABASE(String database){
        DATABASE = database;
    }

}
