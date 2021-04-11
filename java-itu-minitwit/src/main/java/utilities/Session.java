package utilities;

import spark.Request;
import spark.Response;

public class Session {

    private static Request sessionRequest;
    private static Response sessionResponse;

    public static void setSession(Request request, Response response) {
        sessionRequest = request;
        sessionResponse = response;
    }

    public static void clearSessionRequest() {
        sessionRequest = null;
        sessionResponse = null;
    }

    public static Request getSessionRequest() {
        return sessionRequest;
    }

    public static Response getSessionResponse() {
        return sessionResponse;
    }
}
