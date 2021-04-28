package utilities;

import org.json.JSONArray;

public interface IResponses {
    String notFromSimulatorResponse();
    String respond404();
    String respond500();
    String respond403Message(String error);
    String respond404Message(String error);
    String respondLatest();
    String respondFollow(JSONArray json);
    String return404();
    void setStatus(int status);
    void setType(String type);
}
