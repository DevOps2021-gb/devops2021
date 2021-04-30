package utilities;

import errorhandling.Result;
import spark.Request;

import java.util.Map;

public interface IRequests {
    void putAttribute(String attribute, Object value);
    Object getAttribute(String attribute);
    boolean isFromSimulator(String authorization);
    boolean isUserLoggedIn(Integer userid);
    Integer getSessionUserId();
    Object getSessionFlash(Request request);
    Result<Map<String,String>> getFromBody(Request request, String ... args);
    Map<String, String> getFromHeaders(Request request, String ... args);
    Result<String> getParam(String param, Request request);
}
