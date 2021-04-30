package utilities;

import services.ILogService;
import services.MessageService;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static utilities.Session.getSessionRequest;

public class Requests implements IRequests {

    private final IJSONFormatter jsonFormatter;

    public Requests(IJSONFormatter _jsonFormatter) {
        jsonFormatter = _jsonFormatter;
    }

    public void putAttribute(String attribute, Object value) {
        getSessionRequest().session().attribute(attribute, value);
    }

    public Object getAttribute(String attribute) {
        return getSessionRequest().session().attribute(attribute);
    }

    public boolean isFromSimulator(String authorization) {
        return authorization != null && authorization.equals("Basic c2ltdWxhdG9yOnN1cGVyX3NhZmUh");
    }

    public boolean isUserLoggedIn(Integer userid) {
        return userid != null;
    }

    public Integer getSessionUserId() {
        return (Integer) getAttribute(MessageService.USER_ID);
    }

    public Object getSessionFlash(Request request) {
        var msg = request.session().attribute(MessageService.FLASH);
        request.session().removeAttribute(MessageService.FLASH);
        return msg;
    }

    public Result<Map<String,String>> getFromBody(Request request, String ... args){
        try {
            Map<String, String> map = new HashMap<>(request.params());
            addFromParams(map, request, args);
            addFromBody(map, request);
            return new Success<>(map);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    public Map<String, String> getFromHeaders(Request request, String ... args) {
        Map<String, String> map = new HashMap<>();
        if (args.length == 0) {
            for (String p : request.queryParams()) {
                map.put(p, request.queryParams(p));
            }
        } else {
            for (String arg: args) {
                map.put(arg, request.queryParams(arg));
            }
        }
        return map;
    }

    public Result<String> getParam(String param, Request request) {
        var params = getFromBody(request).get();

        if (params.containsKey(param)) {
            return new Success<>(params.get(param));
        } else {
            return new Failure<>(param + " was not found in request");
        }
    }

    private void addFromParams(Map<String, String> map, Request request, String[] args) {
        for (String arg : args) {
            if (request.queryParams(arg) != null) {
                map.put(arg, request.queryParams(arg));
            }
        }
    }

    private void addFromBody(Map<String, String> map, Request request) throws IOException {
        if (!request.body().isEmpty()) {
            if(jsonFormatter.isJSON(request.body())) {
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

    private void addJsonFromBody(Map<String, String> map, Request request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> temp = mapper.readValue(request.body(), Map.class);
        map.putAll(temp);
    }
}
