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
        String msg       = new StringBuilder(className).append(" : Exception ::").append(e.getMessage()).toString();
        logger.log(Level.WARNING, msg);
    }

    public static void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = new StringBuilder(className).append(message).append("  :  ").append(e.getMessage()).toString();
        logger.log(Level.WARNING, msg);
    }
    public static void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = new StringBuilder(className).append("  :  ").append(message).toString();
        logger.log(Level.INFO, msg);
    }

    public static void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        Logger logger = Logger.getLogger(theClass.getSimpleName());
        var params = Requests.getParamsFromRequest(request);
        var body = JSON.formatToJson(params);
        var message = new StringBuilder(theClass.getSimpleName()).append(" : Message :: ").append(request.requestMethod()).append(" ").append(request.url());
        if (!body.equals("")) message.append(" body: ").append(body);
        logger.log(Level.INFO, message.toString());
    }
}
