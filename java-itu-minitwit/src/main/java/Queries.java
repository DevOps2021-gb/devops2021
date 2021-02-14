import Records.Tweet;
import Records.User;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Queries {

    static String DATABASE      = "minitwit.db";
    static final int PER_PAGE         = 30;

    /*
    Returns a new connection to the database.
     */
    public static Result<Connection> connect_db() {
        try{
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                //System.out.println("Connected to " + meta.getDriverName());
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
        Connection c = null;
        try {
            c = connect_db().get();
            ScriptRunner sr = new ScriptRunner(c);

            sr.runScript(new FileReader("schema.sql"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(c);
        }
    }

    /*
    Format a timestamp for display.
    */
    static Result<String> format_datetime(String timestamp) {
        try {
            //System.out.println("TIMESTAMP " + timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(timestamp);

            //System.out.println("FORMATTED " + date.toString());

            return new Success<>(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    static Result<User> querySingleUser(String query, String ... args) {
        Connection conn = null;
        try{
            conn = connect_db().get();
            PreparedStatement  stmt = conn.prepareStatement(query);

            //PreparedStatement indices starts at 1
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i+1, args[i]);
            }

            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) return new Failure<>("No user found for " + query + " args " + Arrays.asList(args));

            int user_id = rs.getInt("user_id");
            String username = rs.getString("username");
            String email = rs.getString("email");
            String pw_hash = rs.getString("pw_hash");

            return new Success<>(new User(user_id,username,email,pw_hash));
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    public static Result<Boolean> following(int who_id, int whom_id) {
        Connection conn = null;
        try {
            conn = connect_db().get();
            var stmt = conn.prepareStatement("select 1 from follower where follower.who_id = ? and follower.whom_id = ?");

            stmt.setInt(1, who_id);
            stmt.setInt(2, whom_id);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                return new Success<>(true);
            } else {
                return new Success<>(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    /*
    Convenience method to look up the id for a username.
     */
    public static Result<Integer> getUserId(String username) {
        var user = getUser(username);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get().user_id());
    }

    public static Result<User> getUser(String username) {
        var user = querySingleUser("select * from user where username = ?", username);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get());
    }

    public static Result<User> getUserById(int user_id) {
        var user = querySingleUser("select * from user where user_id = ?", user_id + "");

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get());
    }


    /*
    Return the gravatar image for the given email address.
    */
    static String gravatar_url(String email) {
        String encodedEmail = new String(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String hashHex = Hashing.generate_hash_hex(encodedEmail);
        return String.format("http://www.gravatar.com/avatar/%s?d=identicon&s=%d", hashHex, 50);
    }

    /*
    Current user follow username
    */
    static Result<String> follow_user(int who_id, String whom_username) {
        Result<User> who_user = getUserById(who_id);
        Result<Integer> whom_id = getUserId(whom_username);

        if (!who_user.isSuccess()) {
            return new Failure<>(who_user.toString());
        } else if (!whom_id.isSuccess()) {
            return new Failure<>(whom_id.toString());
        } else {
            Connection conn = null;
            try {
                conn = connect_db().get();
                var stmt = conn.prepareStatement("insert into follower (who_id, whom_id) values (?, ?)");

                stmt.setInt(1, who_user.get().user_id());
                stmt.setInt(2, whom_id.get());

                stmt.execute();

                return new Success<>("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            } finally {
                closeConnection(conn);
            }
        }
    }

    /*
    Current user unfollow user
    */
    static Result<String> unfollow_user(int who_id, String whom_username) {
        Result<User> who_user = getUserById(who_id);
        Result<Integer> whom_id = getUserId(whom_username);

        if (!who_user.isSuccess()) {
            return new Failure<>(who_user.toString());
        } else if (!whom_id.isSuccess()) {
            return new Failure<>(whom_id.toString());
        } else {
            Connection conn = null;
            try {
                conn = connect_db().get();
                var stmt = conn.prepareStatement("delete from follower where who_id=? and whom_id=?");

                stmt.setInt(1, who_user.get().user_id());
                stmt.setInt(2, whom_id.get());

                stmt.execute();

                return new Success<>("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            } finally {
                closeConnection(conn);
            }
        }
    }

    /*
    Displays the latest messages of all users.
    */
    public static Result<ArrayList<Tweet>> public_timeline() {
        Connection conn = null;
        try{
            conn = connect_db().get();
            PreparedStatement  stmt = conn.prepareStatement("""
                select message.*, user.* from message, user
                where message.flagged = 0 and message.author_id = user.user_id
                order by message.pub_date desc limit ?""");

            stmt.setInt(1, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                String username         = rs.getString("username");
                String email            = rs.getString("email");
                int pub_date            = rs.getInt("pub_date");
                String formatted_date   = format_datetime(String.valueOf(pub_date)).get();
                String text             = rs.getString("text");

                tweets.add(new Tweet(email, username, text, formatted_date, gravatar_url(email)));
            }

            return new Success<>(tweets);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    public static Result<ArrayList<Tweet>> getTweetsByUsername(String username) {
        Connection conn = null;
        try{
            var user = getUser(username);
            conn = connect_db().get();
            PreparedStatement  stmt = conn.prepareStatement("""
            select message.*, user.* from message, user
                where message.flagged = 0 and message.author_id = user.user_id
                and user.user_id = ?
                order by message.pub_date desc limit ?""");

            stmt.setInt(1, user.get().user_id());
            stmt.setInt(2, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                int pub_date = rs.getInt("pub_date");
                String formatted_date = format_datetime(String.valueOf(pub_date)).get();
                String text = rs.getString("text");
                String email = user.get().email();

                tweets.add(new Tweet(email, user.get().username(), text, formatted_date, gravatar_url(email)));
            }

            return new Success<>(tweets);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    public static Result<ArrayList<Tweet>> getPersonalTweetsById(int user_id) {
        Connection conn = null;
        try{
            conn = connect_db().get();
            PreparedStatement  stmt = conn.prepareStatement("""
             select message.*, user.* from message, user
                    where message.flagged = 0 and message.author_id = user.user_id and (
                        user.user_id = ? or
                        user.user_id in (select whom_id from follower
                                                where who_id = ?))
                    order by message.pub_date desc limit ?""");

            stmt.setInt(1, user_id);
            stmt.setInt(2, user_id);
            stmt.setInt(3, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                int pub_date = rs.getInt("pub_date");
                String formatted_date = format_datetime(String.valueOf(pub_date)).get();
                String text = rs.getString("text");
                String email = rs.getString("email");
                String username = rs.getString("username");

                tweets.add(new Tweet(email, username, text, formatted_date, gravatar_url(email)));
            }
            return new Success<>(tweets);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    /*
        Registers a new message for the user.
    */
    public static Result<Integer> add_message(String text, int loggedInUserId) {
        if (!text.equals("")) {
            Connection conn = null;
            try{
                conn = connect_db().get();
                PreparedStatement  stmt = conn.prepareStatement("insert into message (author_id, text, pub_date, flagged) values (?, ?, ?, 0)");
                long timestamp = (int) new Date().getTime();

                stmt.setInt(1, loggedInUserId);
                stmt.setString(2, text);
                stmt.setString(3, timestamp + "");

                int res = stmt.executeUpdate();
                return new Success<>(res);
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            } finally {
                closeConnection(conn);
            }
        }
        return new Failure<>("You need to add text to the message");
    }

    static Result<String> queryLogin(String username, String password) {
        String error;
        var user = querySingleUser("select * from user where username = ?", username);
        if (!user.isSuccess()) {
            error = "Invalid username";
        } else if (!Hashing.check_password_hash(user.get().pw_hash(), password)) {
            error = "Invalid password";
        } else {
            System.out.println("You were logged in");
            return new Success<>("login successful");
        }

        return new Failure<>(error);
    }

    static Result<String> register(String username, String email, String password1, String password2) {
        String error = "";
        if (username == null || username.equals("")) {
            error = "You have to enter a username";
        } else if (email == null || !email.contains("@")) {
            error = "You have to enter a valid email address";
        } else if (password1 == null || password1.equals("")) {
            error = "You have to enter a password";
        } else if (!password1.equals(password2)) {
            error = "The two passwords do not match";
        } else if (getUserId(username).isSuccess()) {
            error = "The username is already taken";
        } else {
            try {
                var conn = connect_db();
                if(conn.isSuccess()) {
                    PreparedStatement stmt = conn.get().prepareStatement("insert into user (username, email, pw_hash) values (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, Hashing.generate_password_hash(password1));

                    stmt.executeUpdate();
                    closeConnection(conn.get());

                    System.out.println("You were successfully registered and can login now");
                    return new Success<>("OK");
                }

            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            }
        }
        return new Failure<>(error);
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void setDATABASE(String dbName) {
        DATABASE = dbName;
    }
}
