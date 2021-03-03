import Model.Follower;
import Model.Message;
import Model.Tweet;
import Model.User;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import org.hibernate.criterion.Restrictions;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Queries {

    static final int PER_PAGE = 30;

    /*
    Creates the database tables.
     */
    public static void initDb()  {
        var db = DB.connectDb().get();
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

    public static Result<Boolean> isFollowing(int whoId, int whomId) {
        try {
            var db = DB.connectDb().get();
            List<Follower> result = db.createQuery("from Follower f where f.who.id=:whoId AND f.whom.id=:whomId")
                    .setInteger("whoId", whoId).setInteger("whomId", whomId).list();
            //var stmt = conn.prepareStatement("select 1 from follower where follower.whoId = ? and follower.whomId = ?");

            return new Success<>(!result.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
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
        var result = (List<User>) db.createCriteria(User.class).add(Restrictions.eq("username", username)).list();

        if (result == null || result.size() == 0) return new Failure<>("No user found for " + username);

        return new Success<>(result.get(0));
    }

    public static Result<User> getUserById(int userId) {
        var db = DB.connectDb().get();
        var result = (User) db.get(User.class, userId);

        if (result == null) return new Failure<>("No user found for id " + userId);

        return new Success<>(result);
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
    static Result<Follower> followUser(int whoId, String whomUsername) {
        Result<User> whoUser = getUserById(whoId);
        Result<User> whomUser= getUser(whomUsername);

        if (!whoUser.isSuccess()) {
            return new Failure<>(whoUser.toString());
        } else if (!whomUser.isSuccess()) {
            return new Failure<>(whomUser.toString());
        } else {
            try {
                var db = DB.connectDb().get();
                db.beginTransaction();  //todo test if needed
                var follower = new Follower(whoUser.get(), whomUser.get());
                db.save(follower);
                db.getTransaction().commit();

                return new Success<>(follower);
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
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
            try {
                var db = DB.connectDb().get();
                db.beginTransaction();
                List<Follower> followersToDelete = db.createQuery("from Follower f where f.who.id=:whoId AND f.whom.id=:whomId")
                        .setInteger("whoId", whoId).setInteger("whomId", whomId.get()).list();
                for(var follower: followersToDelete) {
                    db.delete(follower);       //toto: test if it works
                }
                db.getTransaction().commit();
                //db.table("follower").where("whoId=?", whoId).where("whomId=?", whomId.get()).delete();

                return new Success<>("OK");
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            }
        }
    }

    static Result<List<User>> getFollowing(int whoId) {
        try{
            var db = DB.connectDb().get();
            List<Object[]> test = db.createQuery("from Follower f inner join f.who u where u.id=:whoId")
                    .setInteger("whoId", whoId).setMaxResults(PER_PAGE).list();
            List<User> result = test.stream().map(o->((Follower) o[0]).getWhom()).collect(Collectors.toList());;
            return new Success<>(result);
        } catch (Exception e) {
            e.printStackTrace();
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

    /*
    Displays the latest messages of all users.
    */
    public static Result<List<Tweet>> publicTimeline() {
        try{
            var db = DB.connectDb().get();
            /* todo
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                "where message.flagged = 0 and message.authorId = user.id " +
                "order by message.pubDate desc limit ?", PER_PAGE).results(HashMap.class);
             */
            List<HashMap> result = null;
            return new Success<>(tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    public static Result<List<Tweet>> getTweetsByUsername(String username) {
        try{
            var userId = getUserId(username);
            var db = DB.connectDb().get();
            /* todo
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                "where message.flagged = 0 and message.authorId = user.id " +
                "and user.id = ? " +
                "order by message.pubDate desc limit ?", userId.get(), PER_PAGE).results(HashMap.class);
            */
            List<HashMap> result = null;
            return new Success<>(tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    public static Result<List<Tweet>> getPersonalTweetsById(int userId) {
        try{
            var db = DB.connectDb().get();
            /* todo
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                    "where message.flagged = 0 and message.authorId = user.id and (" +
                        "user.id = ? or " +
                        "user.id in (select whomId from follower where whoId = ?)) " +
                    "order by message.pubDate desc limit ?", userId, userId, PER_PAGE).results(HashMap.class);
            */
            List<HashMap> result = null;
            return new Success<>(tweetsFromListOfHashMap(result));

        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
        Registers a new message for the user.
    */
    public static Result<Boolean> addMessage(String text, int loggedInUserId) {
        if (!text.equals("")) {
            try{
                long timestamp = new Date().getTime();
                var db = DB.connectDb().get();
                User user = getUserById(loggedInUserId).get();
                db.save(new Message(user, text, timestamp, 0));

                return new Success<>(true);
            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            }
        }
        return new Failure<>("You need to add text to the message");
    }

    static Result<Boolean> queryLogin(String username, String password) {
        String error;
        var user = getUser(username);
        if (!user.isSuccess()) {
            error = "Invalid username";
        } else if (!Hashing.checkPasswordHash(user.get().pwHash, password)) {
            error = "Invalid password";
        } else {
            System.out.println("You were logged in");
            return new Success<>(true);
        }

        return new Failure<>(error);
    }

    static Result<User> register(String username, String email, String password1, String password2) {
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
                var db = DB.connectDb().get();
                db.beginTransaction();
                User user = new User(username,email, Hashing.generatePasswordHash(password1));
                db.save(user);
                db.getTransaction().commit();

                System.out.println("You were successfully registered and can login now");
                return new Success<>(user);


            } catch (Exception e) {
                e.printStackTrace();
                return new Failure<>(e);
            }
        }
        return new Failure<>(error);
    }
}
