package utilities;

import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import services.UserService;

import static utilities.JSON.APPLICATION_JSON;
import static utilities.Session.getSessionResponse;

public class Responses {

    private Responses() {}

    private static final String MESSAGE404_NOT_FOUND = "{\"message\":\"404 not found\"}";
    private static final String MESSAGE500_SERVER_ERROR = "{\"message\":\"500 server error\"}";

    public static String notFromSimulatorResponse() {
        Responses.setStatus(HttpStatus.FORBIDDEN_403);
        Responses.setType(APPLICATION_JSON);
        return respond403Message("You are not authorized to use this resource!");
    }

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

    public static String respondLatest() {
        return "{\"latest\":" + UserService.latest + "}";
    }

    public static String respondFollow(JSONArray json) {
        return "{\"follows\": " + json + " }";
    }

    public static String return404(){
        Responses.setStatus(HttpStatus.NOT_FOUND_404);
        Responses.setType(APPLICATION_JSON);
        return MESSAGE404_NOT_FOUND;
    }

    public static void setStatus(int status) {
        getSessionResponse().status(status);
    }

    public static void setType(String type) {
        getSessionResponse().type(type);
    }
}