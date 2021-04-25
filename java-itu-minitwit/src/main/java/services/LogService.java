package services;

import spark.Request;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.JSON;
import utilities.Requests;

public class LogService {

    private LogService() {
    }

    public static void logError(Exception e, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(theClass.getSimpleName());
        StringBuilder msg= new StringBuilder(className).append(" : Exception ::").append(e.getMessage());
        log(logger, Level.WARNING, msg);
    }

    public static void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        StringBuilder msg= new StringBuilder(className).append(message).append("  :  ").append(e.getMessage());
        log(logger, Level.WARNING, msg);
    }
    public static void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        StringBuilder msg = new StringBuilder(className).append("  :  ").append(message);
        log(logger, Level.INFO, msg);
    }

    public static void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        Logger logger = Logger.getLogger(theClass.getSimpleName());
        var params = Requests.getFromBody(request);
        var queryParams = Requests.getFromHeaders(request);

        if (params.containsKey("password")) params.put("password", "REDACTED");
        if (params.containsKey("password2")) params.put("password2", "REDACTED");

        var body = JSON.formatToJson(params);
        var headers = JSON.formatToJson(queryParams);

        var message = new StringBuilder(theClass.getSimpleName()).append(" : Message :: ").append(request.requestMethod()).append(" ").append(request.url());
        if (!body.equals("")) message.append(" body: ").append(body);
        if (!headers.equals("")) message.append(" headers: ").append(headers);
        log(logger, Level.INFO, message);
    }
    private static void log(Logger logger, Level level, StringBuilder sb) {
        String msg = new StringBuilder("_code_minitwit: ").append(sb.toString()).toString();
        logger.log(level, msg);
    }
}
