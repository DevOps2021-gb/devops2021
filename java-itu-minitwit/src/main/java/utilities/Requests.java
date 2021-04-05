package utilities;

import services.LogService;
import services.MessageService;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Requests {

    private Requests() {}

    public static boolean isFromSimulator(String authorization) {
        return authorization != null && authorization.equals("Basic c2ltdWxhdG9yOnN1cGVyX3NhZmUh");
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

    public static Map<String,String> getParamsFromRequest(Request request, String ... args){
        Map<String, String> map = new HashMap<>(request.params());
        addFromParams(map, request, args);
        addFromBody(map, request);
        return map;
    }

    private static void addFromParams(Map<String, String> map, Request request, String[] args) {
        for (String arg : args) {
            if (request.queryParams(arg) != null) {
                map.put(arg, request.queryParams(arg));
            }
        }
    }

    private static void addFromBody(Map<String, String> map, Request request) {
        if (!request.body().isEmpty()) {
            if(JSON.isJSON(request.body())) {
                addJsonFromBody(map, request);
            }
            else {
                for(String keyValue : request.body().split(" *& *")) {
                    String[] pairs = keyValue.split(" *= *", 2);
                    map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                }
            }
        }
    }

    private static void addJsonFromBody(Map<String, String> map, Request request) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> temp = mapper.readValue(request.body(), Map.class);
            map.putAll(temp);
        } catch (IOException e) {
            LogService.logError(e);
        }
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
