package services;

import spark.Request;
import java.util.logging.Level;
import java.util.logging.Logger;

import utilities.IJSONFormatter;
import utilities.IRequests;

public class LogService implements ILogService {

    private final IJSONFormatter jsonFormatter;
    private final IRequests requests;

    public LogService(IJSONFormatter jsonFormatter, IRequests requests) {
        this.jsonFormatter = jsonFormatter;
        this.requests = requests;
    }

    public void logError(Exception e, Class theClass) {
        String className = theClass.getSimpleName();
        var logger    = Logger.getLogger(theClass.getSimpleName());
        StringBuilder msg= new StringBuilder(className).append(" : Exception ::").append(e.getMessage());
        log(logger, Level.WARNING, msg);
    }

    public void logErrorWithMessage(Exception e, String message, Class theClass) {
        String className = theClass.getSimpleName();
        var logger    = Logger.getLogger(className);
        StringBuilder msg= new StringBuilder(className).append(message).append("  :  ").append(e.getMessage());
        log(logger, Level.WARNING, msg);
    }
    public void log(Class theClass, String message) {
        String className = theClass.getSimpleName();
        var logger    = Logger.getLogger(className);
        StringBuilder msg = new StringBuilder(className).append("  :  ").append(message);
        log(logger, Level.INFO, msg);
    }

    public void logRequest(Request request, Class theClass) {
        if (request.url().contains("favicon.ico")) return;

        var logger = Logger.getLogger(theClass.getSimpleName());
        var params = requests.getFromBody(request).get();
        var queryParams = requests.getFromHeaders(request);

        if (params.containsKey("password")) params.put("password", "REDACTED");
        if (params.containsKey("password2")) params.put("password2", "REDACTED");

        var body = jsonFormatter.formatToJson(params);
        var headers = jsonFormatter.formatToJson(queryParams);

        var message = new StringBuilder(theClass.getSimpleName()).append(" : Message :: ").append(request.requestMethod()).append(" ").append(request.url());
        if (!body.equals("")) message.append(" body: ").append(body);
        if (!headers.equals("")) message.append(" headers: ").append(headers);
        log(logger, Level.INFO, message);
    }

    private static void log(Logger logger, Level level, StringBuilder sb) {
        String msg = "_code_minitwit: " + sb.toString();
        logger.log(level, msg);
    }
}
