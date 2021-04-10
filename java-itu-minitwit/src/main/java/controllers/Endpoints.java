package controllers;

import services.*;
import persistence.UserRepository;
import utilities.JSON;
import utilities.Requests;
import view.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Endpoints {

    private Endpoints() {}

    private static final String REGISTER        = "/register";
    private static final String FLLWS_USERNAME  = "/fllws/:username";
    private static final String MSGS_USERNAME   = "/msgs/:username";
    private static final String LOGIN           = "/login";
    private static final String ADD_MESSAGE     = "/add_message";

    private static final String LATESTS         = "/latest";
    private static final String MESSAGES        = "/msgs";
    private static final String TIMELINE        = "/";
    private static final String PUBLIC_TIMELINE = "/public";
    private static final String USER_TIMELINE   = "/:username";
    private static final String METRICS         = "/metrics";
    private static final String LOGOUT          = "/logout";
    private static final String FOLLOW          = "/:username/follow";
    private static final String UNFOLLOW        = "/:username/unfollow";

    public static void registerEndpoints(){
        var postEndpoints = new String[] {MSGS_USERNAME, FLLWS_USERNAME, ADD_MESSAGE, LOGIN, REGISTER};
        var getEndpoints  = new String[] {LATESTS, MESSAGES, MSGS_USERNAME, FLLWS_USERNAME, TIMELINE, METRICS, PUBLIC_TIMELINE, LOGIN, REGISTER, LOGOUT, FOLLOW, UNFOLLOW, USER_TIMELINE};
        MaintenanceService.setEndpointsToLog(getEndpoints, postEndpoints);

        Spark.post(MSGS_USERNAME,             Endpoints::addMessage);
        Spark.post(FLLWS_USERNAME,            Endpoints::postFollow);
        Spark.post(ADD_MESSAGE,               Endpoints::addMessage);
        Spark.post(LOGIN,                     Endpoints::login);
        Spark.post(REGISTER,                  Endpoints::register);

        Spark.get(LATESTS,                    Endpoints::getLatest);
        Spark.get(MESSAGES,                   Endpoints::messages);
        Spark.get(MSGS_USERNAME,              Endpoints::messagesPerUser);
        Spark.get(FLLWS_USERNAME,             Endpoints::getFollow);
        Spark.get(TIMELINE,                   Endpoints::timeline);
        Spark.get(METRICS,                    Endpoints::metrics);
        Spark.get(PUBLIC_TIMELINE,            Endpoints::publicTimeline);
        Spark.get(LOGIN,                      Endpoints::loginGet);
        Spark.get(REGISTER,                   (req, res)-> Presentation.renderTemplate(MessageService.REGISTER_HTML));
        Spark.get(LOGOUT,                     Endpoints::logout);
        Spark.get(FOLLOW,                     Endpoints::followUser);
        Spark.get(UNFOLLOW,                   Endpoints::unfollowUser);
        Spark.get(USER_TIMELINE,              Endpoints::userTimeline);
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
            MaintenanceService.processRequest();
            LogService.logRequest(request, Endpoints.class);

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
