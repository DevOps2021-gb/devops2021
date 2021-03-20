package Persistence;

import Service.MessageService;
import Model.Message;
import Model.Tweet;
import RoP.Failure;
import RoP.Result;
import RoP.Success;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageRepository {
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
        return getTweetsFromMessageUser("and user.id = ? ", userId.get());
    }

    public static Result<List<Tweet>> getPersonalTweetsById(int userId) {
        return getTweetsFromMessageUser("and (user.id = ? or user.id in (select whomId from follower where whoId = ?)) ", userId, userId);
    }

    private static Result<List<Tweet>> getTweetsFromMessageUser(String condition, Object... args){
        try{
            var db = DB.connectDb().get();
            List<HashMap> result = db.sql(
                    "select message.*, user.* from message, user " +
                            "where message.flagged = 0 and message.authorId = user.id " +
                            condition +" "+
                            "order by message.pubDate desc limit "+PER_PAGE, args).results(HashMap.class);
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
