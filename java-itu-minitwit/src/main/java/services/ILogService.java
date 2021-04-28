package services;

import spark.Request;

public interface ILogService {
    void logError(Exception e, Class theClass);
    void logErrorWithMessage(Exception e, String message, Class theClass);
    void log(Class theClass, String message);
    void logRequest(Request request, Class theClass);
}
