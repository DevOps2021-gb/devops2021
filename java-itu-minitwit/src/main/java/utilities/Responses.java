package utilities;

import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import services.UserService;

import static utilities.JSONFormatter.APPLICATION_JSON;
import static utilities.Session.getSessionResponse;

public class Responses implements IResponses {

    public Responses() {}

    private static final String MESSAGE404_NOT_FOUND = "{\"message\":\"404 not found\"}";
    private static final String MESSAGE500_SERVER_ERROR = "{\"message\":\"500 server error\"}";

    public String notFromSimulatorResponse() {
        setStatus(HttpStatus.FORBIDDEN_403);
        setType(APPLICATION_JSON);
        return respond403Message("You are not authorized to use this resource!");
    }

    public String respond404() {
        return MESSAGE404_NOT_FOUND;
    }

    public String respond500() {
        return MESSAGE500_SERVER_ERROR;
    }

    public String respond403Message(String error) {
        return "{\"status\": 403, \"error_msg\": " + error + " }";
    }

    public String respond404Message(String error) {
        return "{\"message\":\"203\", \"error_msg\": "+ error + "}";
    }

    public String respondLatest() {
        return "{\"latest\":" + UserService.latest + "}";
    }

    public String respondFollow(JSONArray json) {
        return "{\"follows\": " + json + " }";
    }

    public String return404(){
        setStatus(HttpStatus.NOT_FOUND_404);
        setType(APPLICATION_JSON);
        return MESSAGE404_NOT_FOUND;
    }

    public void setStatus(int status) {
        getSessionResponse().status(status);
    }

    public void setType(String type) {
        getSessionResponse().type(type);
    }
}
