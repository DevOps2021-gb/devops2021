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
        String msg       = className + " : Exception ::" + e.getMessage();
        logger.log(Level.WARNING, msg);
    }

    public static void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = className + message + "  :  " + e.getMessage();
        logger.log(Level.WARNING, msg);
    }
    public static void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = className + "  :  " + message;
        logger.log(Level.INFO, msg);
    }

    public static void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        Logger logger = Logger.getLogger(theClass.getSimpleName());
        var params = Requests.getBody(request);
        var queryParams = Requests.getHeaders(request);

        var body = JSON.formatToJson(params);
        var headers = JSON.formatToJson(queryParams);

        var message = new StringBuilder(theClass.getSimpleName()).append(" : Message :: ").append(request.requestMethod()).append(" ").append(request.url());
        if (!body.equals("")) message.append(" body: ").append(body);
        if (!headers.equals("")) message.append(" headers: ").append(headers);
        logger.log(Level.INFO, message.toString());
    }
}
