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
    private static final String MESSAGE404_NOT_FOUND = "{\"message\":\"404 not found\"}";
    private static final String MESSAGE500_SERVER_ERROR = "{\"message\":\"500 server error\"}";

    public static String respond404() {
        return MESSAGE404_NOT_FOUND;
    }

    public static String respond500() {
        return MESSAGE500_SERVER_ERROR;
    }

    public static String respond403Message(String error) {
        return "{\"status\": 403, \"error_msg\": " + error + " }";
    }

    public static String respond404Message(String error) {
        return "{\"message\":\"203\", \"error_msg\": "+ error + "}";
    }

    public static String respondLatest(int latest) {
        return "{\"latest\":" + latest + "}";
    }

    public static String respondFollow(JSONArray json) {
        return "{\"follows\": " + json + " }";
    }

    public static boolean isJSON(String body) {
        return body.startsWith("{");
    }

    public static String return404(Response response){
        response.status(HttpStatus.NOT_FOUND_404);
        response.type(APPLICATION_JSON);
        return MESSAGE404_NOT_FOUND;
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
