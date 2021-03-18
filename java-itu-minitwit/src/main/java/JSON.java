import org.json.JSONArray;

public class JSON {
    // responses
    public static final String APPLICATION_JSON = "application/json";
    public static final String MESSAGE404_NOT_FOUND = "{\"message\":\"404 not found\"}";
    public static final String MESSAGE500_SERVER_ERROR = "{\"message\":\"500 server error\"}";

    public static String Respond403(String error) {
        return "{\"status\": 403, \"error_msg\": " + error + " }";
    }

    public static String Respond404(String error) {
        return "{\"message\":\"404 not found\", \"error_msg\": "+ error + "}";
    }

    public static String RespondLatest(int latest) {
        return "{\"latest\":" + latest + "}";
    }

    public static String RespondFollow(JSONArray json) {
        return "{\"follows\": " + json + " }";
    }
}
