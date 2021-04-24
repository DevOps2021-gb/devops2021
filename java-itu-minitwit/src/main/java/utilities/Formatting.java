package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import model.Tweet;

import java.text.SimpleDateFormat;
import java.util.*;

public class Formatting {

    private Formatting() {}

    private static final String USERNAME = "username";
    private static final String EMAIL    = "email";

    private static final String TIMESTAMP_PATTERN = "dd-MM-yyy HH:mm a";

    public static Result<String> formatDatetime(String timestamp) {
        try {
            Date resultDate = new Date(Long.parseLong(timestamp));
            SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.ENGLISH);
            String formattedDate = sdf.format(resultDate);

            return new Success<>(formattedDate);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public static List<Tweet> tweetsFromListOfHashMap(List<HashMap> result){
        List<Tweet> tweets = new ArrayList<>();
        for (HashMap hm: result) {
            String email        = (String) hm.get(EMAIL);
            String username     = (String) hm.get(USERNAME);
            String text         = (String) hm.get("text");
            String pubDate      = Formatting.formatDatetime((long) hm.get("pubDate") + "").get();
            String profilePic   = Hashing.getGravatarUrl(email);
            tweets.add(new Tweet(email, username, text, pubDate, profilePic));
        }
        return tweets;
    }
}
