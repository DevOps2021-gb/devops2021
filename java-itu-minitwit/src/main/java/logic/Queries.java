package logic;

import model.Follower;
import model.Message;
import model.Tweet;
import model.User;
import rop.Failure;
import rop.Result;
import rop.Success;
import com.dieselpoint.norm.Database;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Queries {

    static final int PER_PAGE = 30;

    private Queries() {}

    /*
    Creates the database tables.
     */
    public static Database initDb()  {
        return DB.connectDb().get();
    }
    public static Database getDB()  {
        return DB.connectDb().get();
    }
    public static void dropDB(){
        var db = initDb();
        db.sql("drop table if exists follower").execute();
        db.sql("drop table if exists message").execute();
        db.sql("drop table if exists user").execute();

        db.createTable(User.class);
        db.createTable(Message.class);
        db.sql("ALTER TABLE message ADD FOREIGN KEY (authorId) REFERENCES user(id)").execute();
        db.createTable(Follower.class);
        db.sql("ALTER TABLE follower ADD FOREIGN KEY (whoId) REFERENCES user(id)").execute();
        db.sql("ALTER TABLE follower ADD FOREIGN KEY (whomId) REFERENCES user(id)").execute();
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
            return new Failure<>(e);
        }
    }

    public static Result<Boolean> isFollowing(int whoId, int whomId) {
        try {
            var db = getDB();
            var result = db.where("whoId=?", whoId).where("whomId=?", whomId).results(Follower.class);
            return new Success<>(!result.isEmpty());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    /*
    Convenience method to look up the id for a username.
     */
    public static Result<Integer> getUserId(String username) {
        var user = getUser(username);

        if (!user.isSuccess()) return new Failure<>(user.toString());

        return new Success<>(user.get().id);
    }

    public static Result<User> getUser(String username) {
        var db = getDB();
        var result = db.table("user").where("username=?", username).first(User.class);

        if (result == null) return new Failure<>("No user found for " + username);

        return new Success<>(result);
    }
    public static Result<Long> getCountUsers() {
        var db = getDB();
        var result = db.sql("select count(*) from user").first(Long.class);
        return new Success<>(result);
    }
    public static Result<Long> getCountFollowers() {
        var db = getDB();
        var result = db.sql("select count(*) from follower").first(Long.class);
        return new Success<>(result);
    }
    public static Result<Long> getCountMessages() {
        var db = getDB();
        var result = db.sql("select count(*) from message").first(Long.class);
        return new Success<>(result);
    }

    public static Result<User> getUserById(int userId) {
        var db = getDB();
        var result = db.where("id=?", userId).first(User.class);

        if (result == null) return new Failure<>("No user found for id " + userId);

        return new Success<>(result);
    }


    /*
    Return the gravatar image for the given email address.
    */
    public static String gravatarUrl(String email) {
        String encodedEmail = new String(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        String hashHex = Hashing.generateHashHex(encodedEmail);
        return String.format("http://www.gravatar.com/avatar/%s?d=identicon&s=%d", hashHex, 50);
    }

    /*
    Current user follow username
    */
    public static Result<String> followUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = getDB();
                db.insert(new Follower(whoId, whomId.get()));

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }

    /*
    Current user unfollow user
    */
    public static Result<String> unfollowUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = getDB();
                db.table("follower").where("whoId=?", whoId).where("whomId=?", whomId.get()).delete();

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }

    public static Result<List<User>> getFollowing(int whoId) {
        try{
            var db = getDB();

            List<User> result = db.sql(
            "select user.* from user " +
                    "inner join follower on follower.whomId=user.id " +
                   "where follower.whoId=? "+
                   "limit ?", whoId, PER_PAGE).results(User.class);
            return new Success<>(result);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static List<Tweet> tweetsFromListOfHashMap(List<HashMap> result){
        List<Tweet> tweets = new ArrayList<>();
        for (HashMap hm: result) {
            String email        = (String) hm.get("email");
            String username     = (String) hm.get("username");
            String text         = (String) hm.get("text");
            String pubDate      = formatDatetime((long) hm.get("pubDate") + "").get();
            String profilePic   = gravatarUrl(email);
            tweets.add(new Tweet(email, username, text, pubDate, profilePic));
        }
        return tweets;
    }

    private static Result<List<Tweet>> getTweetsFromMessageUser(String condition, Object... args){
        try{
            var db = getDB();
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                            "where message.flagged = 0 and message.authorId = user.id " +
                            condition +" "+
                            "order by message.pubDate desc limit "+PER_PAGE, args).results(HashMap.class);
            return new Success<>(tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }


    /*
    Displays the latest messages of all users.
    */
    public static Result<List<Tweet>> publicTimeline() {
        return getTweetsFromMessageUser("");
    }

    public static Result<List<Tweet>> getTweetsByUsername(String username) {
        var userId = getUserId(username);
        return getTweetsFromMessageUser("and user.id = ? ", userId.get());
    }

    public static Result<List<Tweet>> getPersonalTweetsById(int userId) {
        return getTweetsFromMessageUser("and (user.id = ? or user.id in (select whomId from follower where whoId = ?)) ", userId, userId);
    }

    /*
        Registers a new message for the user.
    */
    public static Result<Boolean> addMessage(String text, int loggedInUserId) {
        if (!text.equals("")) {
            try{
                long timestamp = new Date().getTime();
                var db = getDB();
                db.insert(new Message(loggedInUserId, text, timestamp, 0));

                return new Success<>(true);
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
        return new Failure<>("You need to add text to the message");
    }

    public static Result<Boolean> queryLogin(String username, String password) {
        String error;
        var user = getUser(username);
        if (!user.isSuccess()) {
            error = "Invalid username";
        } else if (!Hashing.checkPasswordHash(user.get().getPwHash(), password)) {
            error = "Invalid password";
        } else {
            return new Success<>(true);
        }

        return new Failure<>(error);
    }

    public static Result<String> register(String username, String email, String password1, String password2) {
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
                var db = getDB();
                db.insert(new User(username,email, Hashing.generatePasswordHash(password1)));
                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
        return new Failure<>(error);
    }
}
