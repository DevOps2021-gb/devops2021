package utilities;

import model.Tweet;

import java.util.List;
import java.util.Map;

public interface IJSONFormatter {
    boolean isJSON(String body);
    Object tweetsToJSONResponse(List<Tweet> tweets);
    String formatToJson(Map<String, String> hm);
}
