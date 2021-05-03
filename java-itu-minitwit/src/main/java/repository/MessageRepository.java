package repository;

import model.Message;
import model.Tweet;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import utilities.IFormatting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageRepository implements IMessageRepository {

    private final IUserRepository userRepository;
    private final IFormatting formatting;

    public MessageRepository(IUserRepository userRepository, IFormatting formatting) {
        this.userRepository = userRepository;
        this.formatting = formatting;
    }

    static final int PER_PAGE = 30;

    /*
    Registers a new message for the user.
    */
    public Result<Boolean> addMessage(String text, int loggedInUserId) {
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
    public Result<List<Tweet>> publicTimeline() {
        return getTweetsFromMessageUser("");
    }

    public Result<List<Tweet>> getTweetsByUsername(String username) {
        var userId = userRepository.getUserId(username);
        if (!userId.isSuccess()) {
            return new Failure<>(userId.getFailureMessage());
        }
        return getTweetsFromMessageUser("and u.id = ? ", userId.get());
    }

    public Result<List<Tweet>> getPersonalTweetsById(int userId) {
        try{
            var db = DB.connectDb().get();
            String query =
                    "select joined.email, joined.username, joined.text, joined.pubDate " +
                            "from ( " +
                            "select u.email as email, u.username as username, m.text as text, m.pubDate as pubDate " +
                            "from message m join user u on m.authorId = u.id " +
                            "where m.flagged = 0 and u.id = ? " +
                            "union " +
                            "select u.email as email, u.username as username, m.text as text, m.pubDate as pubDate " +
                            "from message m " +
                            "join user u on m.authorId = u.id " +
                            "join follower f on f.whoId = ? and f.whomId = u.id " +
                            "where m.flagged = 0 " +
                            ") joined " +
                            "order by joined.pubDate desc limit " + PER_PAGE;
            var result = db.sql( query, userId, userId).results(HashMap.class);
            return new Success<>(formatting.tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public Result<Long> countMessages() {
        var db = DB.connectDb().get();
        var result = db.sql("select count(*) from message").first(Long.class);
        return new Success<>(result);
    }

    private Result<List<Tweet>> getTweetsFromMessageUser(String condition, Object... args){
        try{
            var db = DB.connectDb().get();
            String query =
                    "select u.email, u.username, m.text, m.pubDate " +
                            "from message m join user u on m.authorId = u.id " +
                            "where m.flagged = 0 " +
                            condition + " " +
                            "order by m.pubDate desc limit " + PER_PAGE;
            var result = db.sql( query, args).results(HashMap.class);
            return new Success<>(formatting.tweetsFromListOfHashMap(result));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }
}
