package Controller;

import Service.*;
import Persistence.UserRepository;
import Utilities.JSON;
import Utilities.Requests;
import View.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Endpoints {
    private static final String[] entryPointsGetOrder     = new String[]{"/latest", "/msgs", "/msgs/:username", "/fllws/:username", "/", "/metrics", "/public", "/login", "/register", "/logout", "/:username/follow", "/:username/unfollow","/:username"};
    private static final String[] entryPointsPostOrder    = new String[]{"/msgs/:username","/fllws/:username","/add_message","/login","/register"};

    private static final Map<String, BiFunction<Request, Response, Object>> endpointsGet = new HashMap<>();
    private static final Map<String, BiFunction<Request, Response, Object>> endpointsPost =new HashMap<>();

    private static void setUpEntryPointsMap(){
        endpointsGet.put("/latest",              Endpoints::getLatest);
        endpointsGet.put("/msgs",                Endpoints::messages);
        endpointsGet.put("/msgs/:username",      Endpoints::messagesPerUser);
        endpointsGet.put("/fllws/:username",     Endpoints::getFollow);
        endpointsGet.put("/",                    Endpoints::timeline);
        endpointsGet.put("/metrics",             Endpoints::metrics);
        endpointsGet.put("/public",              Endpoints::publicTimeline);
        endpointsGet.put("/login",               Endpoints::loginGet);
        endpointsGet.put("/register",            (req, res)-> Presentation.renderTemplate(MessageService.REGISTER_HTML));
        endpointsGet.put("/logout",              Endpoints::logout);
        endpointsGet.put("/:username/follow",    Endpoints::followUser);
        endpointsGet.put("/:username/unfollow",  Endpoints::unfollowUser);
        endpointsGet.put("/:username",           Endpoints::userTimeline);

        endpointsPost.put("/msgs/:username",     Endpoints::addMessage);
        endpointsPost.put("/fllws/:username",    Endpoints::postFollow);
        endpointsPost.put("/add_message",        Endpoints::addMessage);
        endpointsPost.put("/login",              Endpoints::login);
        endpointsPost.put("/register",           Endpoints::register);
    }

    public static Object getLatest(Request request, Response response) {
        return MessageService.getLatest(response);
    }

    public static Object messages(Request request, Response response) {
        return MessageService.messages(request, response);
    }

    public static Object messagesPerUser(Request request, Response response) {
        return MessageService.messagesPerUser(request, response);
    }

    public static Object getFollow(Request request, Response response) {
        return UserService.getFollow(request, response);
    }

    public static Object timeline(Request request, Response response) {
        return TimelineService.timeline(request, response);
    }

    public static Object metrics(Request request, Response response) {
        return MetricsService.metrics(response);
    }

    public static Object publicTimeline(Request request, Response response) {
        return TimelineService.publicTimeline(request);
    }

    public static Object loginGet(Request request, Response response) {
        return UserService.loginGet(request);
    }

    public static Object logout(Request request, Response response) {
        UserService.logout(request, response);
        return null;
    }

    public static Object followUser(Request request, Response response) {
        UserService.followUser(request, response);
        return null;
    }

    public static Object unfollowUser(Request request, Response response) {
        UserService.unfollowUser(request, response);
        return null;
    }

    public static Object userTimeline(Request request, Response response) {
        return TimelineService.userTimeline(request);
    }

    public static Object addMessage(Request request, Response response) {
        MessageService.addMessage(request, response);
        return null;
    }

    public static Object postFollow(Request request, Response response) {
        return UserService.postFollow(request, response);
    }

    public static Object login(Request request, Response response) {
        return UserService.login(request, response);
    }

    public static Object register(Request request, Response response) {
        return UserService.register(request, response);
    }

    public static void registerEndpoints() {
        setUpEntryPointsMap();
        for(String point : entryPointsGetOrder) {
            Spark.get(point, (req, res)-> LogService.benchMarkEndpoint(point, endpointsGet.get(point), req, res));
        }
        for(String point : entryPointsPostOrder) {
            Spark.post(point, (req, res)-> LogService.benchMarkEndpoint(point, endpointsPost.get(point), req, res));
        }
        LogService.setEndpointsToLog(entryPointsGetOrder, entryPointsPostOrder);
    }

    public static void registerHooks() {
        Spark.before((request, response) -> {
            LogService.processRequest();
            LogService.logRequest(request);

            Integer userId = Requests.getSessionUserId(request);
            if (userId != null) {
                var user = UserRepository.getUserById(userId);
                if (user.isSuccess()) {
                    request.session().attribute(MessageService.USER_ID, user.get().id);
                }
            }
        });

        Spark.notFound((req, res) -> {
            res.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        });

        Spark.internalServerError((req, res) -> {
            res.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE500_SERVER_ERROR;
        });
    }
}
