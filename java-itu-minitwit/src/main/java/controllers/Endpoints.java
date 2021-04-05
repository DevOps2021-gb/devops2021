package controllers;

import io.prometheus.client.exporter.common.TextFormat;
import model.DTO;
import services.*;
import persistence.UserRepository;
import utilities.JSON;
import utilities.Requests;
import utilities.Responses;
import view.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

import static services.MessageService.CONTENT;
import static services.MessageService.USERNAME;
import static utilities.Requests.getParamFromRequest;
import static utilities.Requests.getParamsFromRequest;

public class Endpoints {

    private Endpoints() {}

    private static final String REGISTER = "/register";
    private static final String FLLWS_USERNAME = "/fllws/:username";
    private static final String MSGS_USERNAME = "/msgs/:username";
    private static final String LOGIN = "/login";

    public static void register() {
        registerEndpoints();
        registerHooks();
    }

    private static void registerEndpoints(){
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

    private static Object getLatest(Request request, Response response) {
        response.type(JSON.APPLICATION_JSON);
        return Responses.respondLatest();
    }

    private static Object messages(Request request, Response response) {
        DTO dto = new DTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.response = response;

        return MessageService.getMessages(dto);
    }

    private static Object messagesPerUser(Request request, Response response) {
        DTO dto = new DTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = getParamFromRequest(":username", request).get();
        dto.response = response;

        return MessageService.messagesPerUser(dto);
    }

    private static Object getFollow(Request request, Response response) {
        DTO dto = new DTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = getParamFromRequest(":username", request).get();
        dto.response = response;

        return UserService.getFollow(dto);
    }

    public static Object timeline(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;
        dto.latest = request.queryParams("latest");

        return TimelineService.timeline(dto);
    }

    private static Object metrics(Request request, Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        return MetricsService.metrics();
    }

    private static Object publicTimeline(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.latest = request.queryParams("latest");

        return TimelineService.publicTimeline(dto);
    }

    private static Object loginGet(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;

        return UserService.loginGet(dto);
    }

    private static Object logout(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;

        UserService.logout(dto);
        return "";
    }

    private static Object followUser(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;
        dto.latest = request.queryParams("latest");

        UserService.followUser(dto);
        return "";
    }

    private static Object unfollowUser(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;
        dto.latest = request.queryParams("latest");

        UserService.unfollowUser(dto);
        return "";
    }

    private static Object userTimeline(Request request, Response response) {
        DTO dto = new DTO();
        dto.latest = request.queryParams("latest");

        return TimelineService.userTimeline(dto);
    }

    private static Object addMessage(Request request, Response response) {
        var params = getParamsFromRequest(request, USERNAME, CONTENT);

        DTO dto = new DTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");
        dto.content = params.get(CONTENT);
        dto.request = request;
        dto.response = response;

        MessageService.addMessage(dto);
        return "";
    }

    private static Object postFollow(Request request, Response response) {
        DTO dto = new DTO();
        dto.username = getParamFromRequest(":username", request).get();
        dto.follow = getParamFromRequest("follow", request);
        dto.unfollow = getParamFromRequest("unfollow", request);
        dto.response = response;

        return UserService.postFollow(dto);
    }

    private static Object login(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;
        dto.latest = request.queryParams("latest");

        return UserService.login(dto);
    }

    private static Object register(Request request, Response response) {
        DTO dto = new DTO();
        dto.request = request;
        dto.response = response;
        dto.latest = request.queryParams("latest");

        return UserService.register(dto);
    }

    private static void registerHooks() {
        Spark.before((request, response) -> {
            LogService.processRequest();

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
            return Responses.respond404();
        });

        Spark.internalServerError((request, response) -> {
            response.type(JSON.APPLICATION_JSON);
            return Responses.respond500();
        });
    }
}
