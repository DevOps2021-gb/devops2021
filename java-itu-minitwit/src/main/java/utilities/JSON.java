package utilities;

import services.MessageService;
import model.Tweet;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSON {

    private JSON() {}

    public static final String APPLICATION_JSON = "application/json";

    public static boolean isJSON(String body) {
        return body.startsWith("{");
    }

    public static Object tweetsToJSONResponse(List<Tweet> tweets) {
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
            Responses.setStatus(HttpStatus.NO_CONTENT_204);
            return "";
        } else {
            Responses.setStatus(HttpStatus.OK_200);
            Responses.setType(JSON.APPLICATION_JSON);
            return json;
        }
    }

    public static String formatToJson(Map<String, String> hm) {
        var sb = new StringBuilder();

        int size = hm.keySet().size();
        int c = 0;

        sb.append("{");
        for (var entry : hm.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue().replaceAll("[\n\r\t]", "_"));

            if (c != size -1) sb.append(", ");

            c++;
        }
        sb.append("}");

        return sb.toString();
    }
}
