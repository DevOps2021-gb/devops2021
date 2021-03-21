package utilities;

import services.LogService;
import services.MessageService;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Requests {

    private Requests() {}

    public static boolean isRequestFromSimulator(Request request) {
        var fromSimulator = request.headers("Authorization");
        return fromSimulator != null && fromSimulator.equals("Basic c2ltdWxhdG9yOnN1cGVyX3NhZmUh");
    }

    public static Object notFromSimulatorResponse(Response response) {
        response.status(HttpStatus.FORBIDDEN_403);
        response.type(JSON.APPLICATION_JSON);
        return JSON.respond403Message("You are not authorized to use this resource!");
    }

    public static boolean isUserLoggedIn(Request request) {
        return getSessionUserId(request) != null;
    }

    public static Integer getSessionUserId(Request request) {
        return request.session().attribute(MessageService.USER_ID);
    }

    public static Object getSessionFlash(Request request) {
        var msg = request.session().attribute(MessageService.FLASH);
        request.session().removeAttribute(MessageService.FLASH);
        return msg;
    }

    //TODO refactor this: Method getParamsFromRequest has a Cognitive Complexity of 13 (exceeds 5 allowed). Consider refactoring
    public static Map<String,String> getParamsFromRequest(Request request, String ... args){
        Map<String, String> map = new HashMap<>(request.params());

        for (String arg : args) {
            if (request.queryParams(arg) != null) {
                map.put(arg, request.queryParams(arg));
            }
        }

        if (!request.body().isEmpty()) {
            if(JSON.isJSON(request.body())) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, String> temp = mapper.readValue(request.body(), Map.class);
                    map.putAll(temp);
                } catch (IOException e) {
                    LogService.logError(e);
                }
            }
            else {
                for(String keyValue : request.body().split(" *& *")) {
                    String[] pairs = keyValue.split(" *= *", 2);
                    map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                }
            }
        }
        return map;
    }

    public static Result<String> getParamFromRequest(String param, Request request) {
        var params = getParamsFromRequest(request);

        if (params.containsKey(param)) {
            return new Success<>(params.get(param));
        } else {
            return new Failure<>(param + " was not found in request");
        }
    }
}
