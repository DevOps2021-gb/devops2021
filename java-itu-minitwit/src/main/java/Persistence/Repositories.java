package Persistence;

import Logic.Minitwit;
import Model.Follower;
import Model.Message;
import Model.Tweet;
import Model.User;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import Utilities.Hashing;
import com.dieselpoint.norm.Database;

import java.util.*;

public class Repositories {
    static final int PER_PAGE = 30;

    private Repositories() {}

    public static Database initDb()  {
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

    public static Result<Boolean> isFollowing(int whoId, int whomId) {
        try {
            var db = DB.connectDb().get();
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
        var db = DB.connectDb().get();
        var result = db.table("user").where("username=?", username).first(User.class);

        if (result == null) return new Failure<>("No user found for " + username);

        return new Success<>(result);
    }
    public static Result<Long> countUsers() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from user").first(Long.class);
        return new Success<>(result);
    }
    public static Result<Long> countFollowers() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from follower").first(Long.class);
        return new Success<>(result);
    }
    public static Result<Long> countMessages() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from message").first(Long.class);
        return new Success<>(result);
    }

    public static Result<User> getUserById(int userId) {
        var db = DB.connectDb().get();
        var result = db.where("id=?", userId).first(User.class);

        if (result == null) return new Failure<>("No user found for id " + userId);

        return new Success<>(result);
    }

    public static Result<String> followUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = DB.connectDb().get();
                db.insert(new Follower(whoId, whomId.get()));

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }

    public static Result<String> unfollowUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<Integer> whomId = getUserId(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomId.isSuccess()) {
            return new Failure<>(whomId.toString());
        } else {
            try {
                var db = DB.connectDb().get();
                db.table("follower").where("whoId=?", whoId).where("whomId=?", whomId.get()).delete();

                return new Success<>("OK");
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
    }

    public static Result<List<User>> getFollowing(int whoId) {
        try{
            var db = DB.connectDb().get();

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

    private static Result<List<Tweet>> getTweetsFromMessageUser(String condition, Object... args){
        try{
            var db = DB.connectDb().get();
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                            "where message.flagged = 0 and message.authorId = user.id " +
                            condition +" "+
                            "order by message.pubDate desc limit "+PER_PAGE, args).results(HashMap.class);
            return new Success<>(Minitwit.tweetsFromListOfHashMap(result));
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
                var db = DB.connectDb().get();
                db.insert(new Message(loggedInUserId, text, timestamp, 0));

                return new Success<>(true);
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }
        return new Failure<>("You need to add text to the message");
    }

    public static Result<String> InsertUser(String username, String email, String password1) {
        try {
            var db = DB.connectDb().get();
            db.insert(new User(username, email, Hashing.generatePasswordHash(password1)));
            return new Success<>("OK");
        } catch (Exception e) {
            return new Failure<>(e);
        }
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
}
