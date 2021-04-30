package repository;

import errorhandling.Result;
import model.Tweet;

import java.util.List;

public interface IMessageRepository {

    Result<Boolean> addMessage(String text, int loggedInUserId);
    Result<List<Tweet>> publicTimeline();
    Result<List<Tweet>> getTweetsByUsername(String username);
    Result<List<Tweet>> getPersonalTweetsById(int userId);
    Result<Long> countMessages();
}
