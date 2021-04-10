package services;

import spark.Request;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogService {
    private LogService() {
    }

    public static void logError(Exception e, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger = Logger.getLogger(theClass.getSimpleName());
        logger.log(Level.WARNING, new StringBuilder(className).append(e.getMessage()).toString());
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
        var sb = new StringBuilder(request.requestMethod()).append(" ");
        if (request.params().size() == 0) {
            sb.append(request.url());
        } else {
            sb.append("with args ").append(request.params().toString().replaceAll("[\n\r\t]", "_"));
        }
        String msg = sb.toString();
        logger.log(Level.INFO, msg);
    }
}
