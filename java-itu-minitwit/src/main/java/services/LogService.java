package services;

import spark.Request;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogService {

    public static void logError(Exception e, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger = Logger.getLogger(theClass.getSimpleName());
        logger.log(Level.WARNING, new StringBuilder(className).append(e.getMessage()).toString());
    }

    public static void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger = Logger.getLogger(className);
        logger.log(Level.WARNING, new StringBuilder(className).append(message).append("  :  ").append(e.getMessage()).toString());
    }
    public static void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        Logger logger = Logger.getLogger(className);
        logger.log(Level.INFO, new StringBuilder(className).append("  :  ").append(message).toString());
    }

    public static void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        Logger logger = Logger.getLogger(theClass.getSimpleName());
        if (request.params().size() == 0) {
            logger.log(Level.INFO, request.requestMethod() + " " + request.url());
        } else {
            logger.log(Level.INFO, request.requestMethod() + " with args " + request.params().toString().replaceAll("[\n\r\t]", "_"));
        }
    }
}
