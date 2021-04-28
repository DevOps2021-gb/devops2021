package services;

import spark.Request;
import java.util.logging.Level;
import java.util.logging.Logger;

import utilities.IJSONFormatter;
import utilities.IRequests;

public class LogService implements ILogService {

    private final IJSONFormatter jsonFormatter;
    private final IRequests requests;

    public LogService(IJSONFormatter _jsonFormatter, IRequests _requests) {
        jsonFormatter = _jsonFormatter;
        requests = _requests;
    }

    public void logError(Exception e, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(theClass.getSimpleName());
        String msg       = className + " : Exception ::" + e.getMessage();
        logger.log(Level.WARNING, msg);
    }

    public void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = className + message + "  :  " + e.getMessage();
        logger.log(Level.WARNING, msg);
    }
    public void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        Logger logger    = Logger.getLogger(className);
        String msg       = className + "  :  " + message;
        logger.log(Level.INFO, msg);
    }

    public void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        Logger logger = Logger.getLogger(theClass.getSimpleName());
        var params = requests.getFromBody(request).get();
        var queryParams = requests.getFromHeaders(request);

        if (params.containsKey("password")) params.put("password", "REDACTED");
        if (params.containsKey("password2")) params.put("password2", "REDACTED");

        var body = jsonFormatter.formatToJson(params);
        var headers = jsonFormatter.formatToJson(queryParams);

        var message = new StringBuilder(theClass.getSimpleName()).append(" : Message :: ").append(request.requestMethod()).append(" ").append(request.url());
        if (!body.equals("")) message.append(" body: ").append(body);
        if (!headers.equals("")) message.append(" headers: ").append(headers);
        logger.log(Level.INFO, message.toString());
    }
}
