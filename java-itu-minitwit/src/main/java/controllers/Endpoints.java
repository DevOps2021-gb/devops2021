package controllers;

import services.*;
import persistence.UserRepository;
import utilities.JSON;
import utilities.Requests;
import view.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Endpoints {

    private Endpoints() {}

    private static final String REGISTER = "/register";
    private static final String FLLWS_USERNAME = "/fllws/:username";
    private static final String MSGS_USERNAME = "/msgs/:username";
    private static final String LOGIN = "/login";

    private static final String[] entryPointsGetOrder     = new String[]{"/latest", "/msgs", MSGS_USERNAME, FLLWS_USERNAME, "/", "/metrics", "/public", LOGIN, REGISTER, "/logout", "/:username/follow", "/:username/unfollow","/:username"};
    private static final String[] entryPointsPostOrder    = new String[]{MSGS_USERNAME,FLLWS_USERNAME,"/add_message",LOGIN,REGISTER};

    private static final Map<String, BiFunction<Request, Response, Object>> endpointsGet = new HashMap<>();
    private static final Map<String, BiFunction<Request, Response, Object>> endpointsPost =new HashMap<>();

    private static void setUpEntryPointsMap(){
        endpointsGet.put("/latest",              Endpoints::getLatest);
        endpointsGet.put("/msgs",                Endpoints::messages);
        endpointsGet.put(MSGS_USERNAME,          Endpoints::messagesPerUser);
        endpointsGet.put(FLLWS_USERNAME,         Endpoints::getFollow);
        endpointsGet.put("/",                    Endpoints::timeline);
        endpointsGet.put("/metrics",             Endpoints::metrics);
        endpointsGet.put("/public",              Endpoints::publicTimeline);
        endpointsGet.put(LOGIN,               Endpoints::loginGet);
        endpointsGet.put(REGISTER,              (req, res)-> Presentation.renderTemplate(MessageService.REGISTER_HTML));
        endpointsGet.put("/logout",              Endpoints::logout);
        endpointsGet.put("/:username/follow",    Endpoints::followUser);
        endpointsGet.put("/:username/unfollow",  Endpoints::unfollowUser);
        endpointsGet.put("/:username",           Endpoints::userTimeline);

        endpointsPost.put(MSGS_USERNAME,         Endpoints::addMessage);
        endpointsPost.put(FLLWS_USERNAME,        Endpoints::postFollow);
        endpointsPost.put("/add_message",        Endpoints::addMessage);
        endpointsPost.put(LOGIN,                 Endpoints::login);
        endpointsPost.put(REGISTER,              Endpoints::register);
    }

    public static Object getLatest(Request request, Response response) {
        return MessageService.getLatest(response);
    }

    public static Object messages(Request request, Response response) {
        return MessageService.getMessages(request, response);
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
            Spark.get(point, (req, res)-> endpointsGet.get(point).apply(req, res));
        }
        for(String point : entryPointsPostOrder) {
            Spark.post(point, (req, res)-> endpointsPost.get(point).apply(req, res));
        }
    }

    public static void registerHooks() {
        Spark.before((request, response) -> {
            //LogService.processRequest();
            //LogService.logRequest(request);

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
            return JSON.respond404();
        });

        Spark.internalServerError((req, res) -> {
            res.type(JSON.APPLICATION_JSON);
            return JSON.respond500();
        });
    }
}
