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

    public static void registerEndpoints(){

        Spark.post(MSGS_USERNAME,             Endpoints::addMessage);
        Spark.post(FLLWS_USERNAME,            Endpoints::postFollow);
        Spark.post("/add_message",       Endpoints::addMessage);
        Spark.post(LOGIN,                     Endpoints::login);
        Spark.post(REGISTER,                  Endpoints::register);

        Spark.get("/latest",             Endpoints::getLatest);
        Spark.get("/msgs",               Endpoints::messages);
        Spark.get(MSGS_USERNAME,              Endpoints::messagesPerUser);
        Spark.get(FLLWS_USERNAME,             Endpoints::getFollow);
        Spark.get("/",                   Endpoints::timeline);
        Spark.get("/metrics",            Endpoints::metrics);
        Spark.get("/public",             Endpoints::publicTimeline);
        Spark.get(LOGIN,                      Endpoints::loginGet);
        Spark.get(REGISTER,                   (req, res)-> Presentation.renderTemplate(MessageService.REGISTER_HTML));
        Spark.get("/logout",             Endpoints::logout);
        Spark.get("/:username/follow",   Endpoints::followUser);
        Spark.get("/:username/unfollow", Endpoints::unfollowUser);
        Spark.get("/:username",          Endpoints::userTimeline);
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
        return "";
    }

    public static Object followUser(Request request, Response response) {
        UserService.followUser(request, response);
        return "";
    }

    public static Object unfollowUser(Request request, Response response) {
        UserService.unfollowUser(request, response);
        return "";
    }

    public static Object userTimeline(Request request, Response response) {
        return TimelineService.userTimeline(request);
    }

    public static Object addMessage(Request request, Response response) {
        MessageService.addMessage(request, response);
        return "";
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

    public static void registerHooks() {
        Spark.before((request, response) -> {
            LogService.processRequest();
            //LogService.logRequest(request);

            if (request.requestMethod().equals("GET")) return;

            Integer userId = Requests.getSessionUserId(request);
            if (userId != null) {
                var user = UserRepository.getUserById(userId);
                if (user.isSuccess()) {
                    request.session().attribute(MessageService.USER_ID, user.get().id);
                }
            }
        });

        Spark.notFound((request, response) -> {
            response.type(JSON.APPLICATION_JSON);
            return JSON.respond404();
        });

        Spark.internalServerError((request, response) -> {
            response.type(JSON.APPLICATION_JSON);
            return JSON.respond500();
        });
    }
}
