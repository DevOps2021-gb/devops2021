import Records.Follower;
import Records.Message;
import Records.Tweet;
import Records.User;
import RoP.Failure;
import RoP.Result;
import RoP.Success;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Queries {

    static String DATABASE      = "minitwit.db";
    static final int PER_PAGE         = 30;

    /*
    Creates the database tables.
     */
    public static void initDb()  {
        var db = DB.connectDb().get();
        db.sql("drop table if exists user").execute();
        db.sql("drop table if exists follower").execute();
        db.sql("drop table if exists message").execute();

        db.createTable(User.class);
        db.createTable(Message.class);
        db.createTable(Follower.class);
    }

    /*
    Format a timestamp for display.
    */
    static Result<String> formatDatetime(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '@' HH:mm");
            Date resultDate = new Date(Long.parseLong(timestamp));
            String date = sdf.format(resultDate);
            return new Success<>(date);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    public static Result<Boolean> following(int whoId, int whomId) {
        try {
            var db = DB.connectDb().get();
            var result = db.where("whoId=?", whoId).where("whomId=?", whomId).results(Follower.class);

            //var stmt = conn.prepareStatement("select 1 from follower where follower.whoId = ? and follower.whomId = ?");

            if (rs.next()) {
                return new Success<>(true);
            } else {
                return new Success<>(true);
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

        return new Success<>(user.get().userId());
    }

    public static Result<User> getUser(String username) {
        var db = DB.connectDb().get();
        var result = db.where("username=?", username).first(User.class);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get());
    }

    public static Result<User> getUserById(int userId) {
        var db = DB.connectDb().get();
        var result = db.where("userId=?", userId).first(User.class);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get());
    }


    /*
    Return the gravatar image for the given email address.
    */
    static String gravatarUrl(String email) {
        String encodedEmail = new String(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String hashHex = Hashing.generateHashHex(encodedEmail);
        return String.format("http://www.gravatar.com/avatar/%s?d=identicon&s=%d", hashHex, 50);
    }

    /*
    Current user follow username
    */
    static Result<String> followUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                conn = connectDb().get();
                var stmt = conn.prepareStatement("insert into follower (whoId, whomId) values (?, ?)");

                stmt.setInt(1, whoUser.get().userId());
                stmt.setInt(2, whomId.get());

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
    static Result<String> unfollowUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            Connection conn = null;
            try {
                conn = connectDb().get();
                var stmt = conn.prepareStatement("delete from follower where whoId=? and whomId=?");

                stmt.setInt(1, whoUser.get().userId());
                stmt.setInt(2, whomId.get());

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
    public static Result<ArrayList<Tweet>> publicTimeline() {
        Connection conn = null;
        try{
            conn = connectDb().get();
            PreparedStatement  stmt = conn.prepareStatement("""
            select message.*, user.* from message, user
                where message.flagged = 0 and message.authorId = user.userId
                order by message.pubDate desc limit ?""");


            stmt.setInt(1, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                String username = rs.getString("username");
                String email = rs.getString("email");
                long pubDate = rs.getLong("pubDate");
                String formattedDate = formatDatetime(String.valueOf(pubDate)).get();
                String text = rs.getString("text");
                tweets.add(new Tweet(email, username, text, formattedDate, gravatarUrl(email)));
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
        try{
            var user = getUser(username);
            conn = connectDb().get();
            PreparedStatement  stmt = conn.prepareStatement("""
            select message.*, user.* from message, user
                where message.flagged = 0 and message.authorId = user.userId
                and user.userId = ?
                order by message.pubDate desc limit ?""");

            stmt.setInt(1, user.get().userId());
            stmt.setInt(2, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                long pubDate = rs.getLong("pubDate");
                String formattedDate = formatDatetime(String.valueOf(pubDate)).get();
                String text = rs.getString("text");
                String email = user.get().email();

                tweets.add(new Tweet(email, user.get().username(), text, formattedDate, gravatarUrl(email)));
            }

            return new Success<>(tweets);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        } finally {
            closeConnection(conn);
        }
    }

    private static Result<ArrayList<Tweet>> getArrayListResult(List<Tweet> result) {
        ArrayList<Tweet> tweets = new ArrayList<>(result);

        for (Tweet t : tweets) {
            t.setPubDate(formatDatetime(String.valueOf(t.getPubDate())).get());
            t.setProfilePic(gravatarUrl(t.getEmail()));
        }

        return new Success<>(tweets);
    }

    public static Result<ArrayList<Tweet>> getPersonalTweetsById(int userId) {
        try{
            conn = connectDb().get();
            PreparedStatement  stmt = conn.prepareStatement("""
             select message.*, user.* from message, user
                    where message.flagged = 0 and message.authorId = user.userId and (
                        user.userId = ? or
                        user.userId in (select whomId from follower
                                                where whoId = ?))
                    order by message.pubDate desc limit ?""");

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, PER_PAGE);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Tweet> tweets = new ArrayList<>();

            while (rs.next()) {
                long pubDate = rs.getLong("pubDate");
                String formattedDate = formatDatetime(String.valueOf(pubDate)).get();
                String text = rs.getString("text");
                String email = rs.getString("email");
                String username = rs.getString("username");

                tweets.add(new Tweet(email, username, text, formattedDate, gravatarUrl(email)));
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
    public static Result<Integer> addMessage(String text, int loggedInUserId) {
        if (!text.equals("")) {
            try{
                conn = connectDb().get();
                PreparedStatement  stmt = conn.prepareStatement("insert into message (authorId, text, pubDate, flagged) values (?, ?, ?, 0)");
                long timestamp = new Date().getTime();

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
        var user = getUser(username);
        if (!user.isSuccess()) {
            error = "Invalid username";
        } else if (!Hashing.checkPasswordHash(user.get().pwHash(), password)) {
            error = "Invalid password";
        } else {
            System.out.println("You were logged in");
            return new Success<>("login successful");
        }

        return new Failure<>(error);
    }

    static Result<String> register(String username, String email, String password1, String password2) {
        String error;
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
                var conn = connectDb();
                if(conn.isSuccess()) {
                    PreparedStatement stmt = conn.get().prepareStatement("insert into user (username, email, pwHash) values (?, ?, ?)");
                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, Hashing.generatePasswordHash(password1));

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
