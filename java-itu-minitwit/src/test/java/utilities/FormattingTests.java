package utilities;

import errorhandling.Failure;
import errorhandling.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import services.MessageService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


class FormattingTests {
    @Test
    void formatDatetimeGivenValidTimestampReturnsSuccess() {
        var time = new Date().getTime();
        var actual = Formatting.formatDatetime(String.valueOf(time));

        Assertions.assertSame(actual.getClass(), Success.class);
    }

    @Test
    void formatDatetimeGivenInvalidTimestampReturnsFailure() {
        var actual = Formatting.formatDatetime("timestamp");

        Assertions.assertSame(actual.getClass(), Failure.class);
    }

    @Test
    void tweetsFromListOfHashMapGivenHashmapReturnsTweets() {
        HashMap hm = new HashMap();
        hm.put("email", "test@test.dk");
        hm.put("username", "test");
        hm.put("text", "body");
        hm.put("pubDate", new Date().getTime());
        var maps = new ArrayList<HashMap>();
        maps.add(hm);

        var actual = Formatting.tweetsFromListOfHashMap(maps);

        Assertions.assertEquals(1, actual.size());
        var tweet = actual.get(0);
        Assertions.assertEquals("test@test.dk", tweet.getEmail());
        Assertions.assertEquals("test", tweet.getUsername());
        Assertions.assertEquals("body", tweet.getText());
        Assertions.assertNotNull(tweet.getPubDate());
        Assertions.assertNotNull(tweet.getProfilePic());
    }
}
