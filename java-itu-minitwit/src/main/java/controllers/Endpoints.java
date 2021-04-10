package controllers;

import io.prometheus.client.exporter.common.TextFormat;
import model.DTO;
import services.*;
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

    public static void register() {
        registerEndpoints();
        registerHooks();
    }

    public static void registerEndpoints(){
        var postEndpoints = new String[] {MSGS_USERNAME, FLLWS_USERNAME, ADD_MESSAGE, LOGIN, REGISTER};
        var getEndpoints  = new String[] {LATESTS, MESSAGES, MSGS_USERNAME, FLLWS_USERNAME, TIMELINE, METRICS, PUBLIC_TIMELINE, LOGIN, REGISTER, LOGOUT, FOLLOW, UNFOLLOW, USER_TIMELINE};
        MaintenanceService.setEndpointsToLog(getEndpoints, postEndpoints);

        Spark.post(MSGS_USERNAME,             (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), MSGS_USERNAME, Endpoints::addMessage));
        Spark.post(FLLWS_USERNAME,            (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), FLLWS_USERNAME, Endpoints::postFollow));
        Spark.post(ADD_MESSAGE,               (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), ADD_MESSAGE, Endpoints::addMessage));
        Spark.post(LOGIN,                     (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), LOGIN, Endpoints::login));
        Spark.post(REGISTER,                  (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), REGISTER, Endpoints::register));

        Spark.get(LATESTS,                    (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LATESTS, Endpoints::getLatest));
        Spark.get(MESSAGES,                   (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), MESSAGES, Endpoints::messages));
        Spark.get(MSGS_USERNAME,              (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), MSGS_USERNAME, Endpoints::messagesPerUser));
        Spark.get(FLLWS_USERNAME,             (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), FLLWS_USERNAME, Endpoints::getFollow));
        Spark.get(TIMELINE,                   (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), TIMELINE, Endpoints::timeline));
        Spark.get(METRICS,                    (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), METRICS, Endpoints::metrics));
        Spark.get(PUBLIC_TIMELINE,            (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), PUBLIC_TIMELINE, Endpoints::publicTimeline));
        Spark.get(LOGIN,                      (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LOGIN, Endpoints::loginGet));
        Spark.get(REGISTER,                   (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), REGISTER, (req2, res2) -> Presentation.renderTemplate(MessageService.REGISTER_HTML)));
        Spark.get(LOGOUT,                     (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LOGOUT, Endpoints::logout));
        Spark.get(FOLLOW,                     (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), FOLLOW, Endpoints::followUser));
        Spark.get(UNFOLLOW,                   (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), UNFOLLOW, Endpoints::unfollowUser));
        Spark.get(USER_TIMELINE,              (req, res)-> MaintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), USER_TIMELINE, Endpoints::userTimeline));
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
            MaintenanceService.processRequest();

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
