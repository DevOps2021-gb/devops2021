package utilities;

import services.MessageService;
import model.Tweet;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSON {

    private JSON() {}

    public static final String APPLICATION_JSON = "application/json";

    public static boolean isJSON(String body) {
        return body.startsWith("{");
    }

    public static Object tweetsToJSONResponse(List<Tweet> tweets, Response response) {
        List<JSONObject> messages = new ArrayList<>();
        for (Tweet t : tweets) {
            HashMap<String, String> msg = new HashMap<>();
            msg.put(MessageService.CONTENT, t.getText());
            msg.put("pub_date", t.getPubDate());
            msg.put(MessageService.USER, t.getUsername());
            messages.add(new JSONObject(msg));
        }
        var json = new JSONArray(messages);
        if (json.length() == 0) {
            response.status(HttpStatus.NO_CONTENT_204);
            return "";
        } else {
            response.status(HttpStatus.OK_200);
            response.type(JSON.APPLICATION_JSON);
            return json;
        }
    }
}
