package Utilities;

import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import spark.Response;

public class JSON {
    // responses
    public static final String APPLICATION_JSON = "application/json";
    public static final String MESSAGE404_NOT_FOUND = "{\"message\":\"404 not found\"}";
    public static final String MESSAGE500_SERVER_ERROR = "{\"message\":\"500 server error\"}";

    public static String respond403Message(String error) {
        return "{\"status\": 403, \"error_msg\": " + error + " }";
    }

    public static String respond404Message(String error) {
        return "{\"message\":\"404 not found\", \"error_msg\": "+ error + "}";
    }

    public static String respondLatest(int latest) {
        return "{\"latest\":" + latest + "}";
    }

    public static String respondFollow(JSONArray json) {
        return "{\"follows\": " + json + " }";
    }

    public static Boolean isJSON(String body) {
        return body.startsWith("{");
    }

    public static String return404(Response response){
        response.status(HttpStatus.NOT_FOUND_404);
        response.type(APPLICATION_JSON);
        return MESSAGE404_NOT_FOUND;
    }
}
