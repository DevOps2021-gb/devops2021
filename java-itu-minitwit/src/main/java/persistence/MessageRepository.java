package persistence;

import services.MessageService;
import model.Message;
import model.Tweet;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageRepository {

    private MessageRepository() {}

    static final int PER_PAGE = 30;

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

    /*
Displays the latest messages of all users.
*/
    public static Result<List<Tweet>> publicTimeline() {
        return getTweetsFromMessageUser("");
    }

    public static Result<List<Tweet>> getTweetsByUsername(String username) {
        var userId = UserRepository.getUserId(username);
        if (!userId.isSuccess()) {
            return new Failure<>(userId.getFailureMessage());
        }
        return getTweetsFromMessageUser("and u.id = ? ", userId.get());
    }

    public static Result<List<Tweet>> getPersonalTweetsById(int userId) {
        //return getTweetsFromMessageUser("and (u.id = ? or u.id in (select f.whomId from follower f where f.whoId = ?)) ", userId, userId);
        try{
            var db = DB.connectDb().get();
            String query =
                    new StringBuilder("select joined.email, joined.username, joined.text, joined.pubDate ")
                            .append("from ( ")
                            .append("select u.email as email, u.username as username, m.text as text, m.pubDate as pubDate ")
                            .append("from message m join user u on m.authorId = u.id ")
                            .append("where m.flagged = 0 and u.id = ? ")
                            .append("union ")
                            .append("select u.email as email, u.username as username, m.text as text, m.pubDate as pubDate ")
                            .append("from message m ")
                            .append("join user u on m.authorId = u.id ")
                            .append("join follower f on f.whoId = ? and f.whomId = u.id ")
                            .append("where m.flagged = 0 ")
                            .append(") joined ")
                            .append("order by joined.pubDate desc limit ").append(PER_PAGE).toString();
            var result = db.sql( query, userId, userId).results(HashMap.class);
            return new Success<>(MessageService.tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    private static Result<List<Tweet>> getTweetsFromMessageUser(String condition, Object... args){
        try{
            var db = DB.connectDb().get();
            String query =
                new StringBuilder("select u.email, u.username, m.text, m.pubDate ")
                    .append("from message m join user u on m.authorId = u.id ")
                    .append("where m.flagged = 0 ")
                    .append(condition).append(" ")
                    .append("order by m.pubDate desc limit ").append(PER_PAGE).toString();
            var result = db.sql( query, args).results(HashMap.class);
            return new Success<>(MessageService.tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static Result<Long> countMessages() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from message").first(Long.class);
        return new Success<>(result);
    }
}
