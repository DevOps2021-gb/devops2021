package controllers;

import io.prometheus.client.exporter.common.TextFormat;
import model.dto.*;
import repository.FollowerRepository;
import repository.UserRepository;
import services.*;
import utilities.JSON;
import utilities.Requests;
import utilities.Responses;
import utilities.Session;
import view.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;

import static services.MessageService.*;
import static services.MessageService.PASSWORD;
import static utilities.Requests.*;

public class Endpoints {

    private Endpoints() {}

    private static final String USR_NAME = ":username";

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

    private static void registerEndpoints(){
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
        var dto = new DTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");

        return MessageService.getMessages(dto);
    }

    private static Object messagesPerUser(Request request, Response response) {
        var dto = new MessagesPerUserDTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = getParam(":username", request).get();

        return MessageService.messagesPerUser(dto);
    }

    private static Object getFollow(Request request, Response response) {
        var dto = new MessagesPerUserDTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = getParam(":username", request).get();

        return UserService.getFollow(dto);
    }

    private static Object timeline(Request request, Response response) {
        var dto = new TimelineDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = getSessionUserId();
        dto.flash = getSessionFlash(request);

        if (!isUserLoggedIn(dto.userId)) {
            return publicTimeline(request, response);
        }

        return TimelineService.timeline(dto);
    }

    private static Object metrics(Request request, Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        return MetricsService.metrics();
    }

    private static Object publicTimeline(Request request, Response response) {
        var dto = new PublicTimelineDTO();
        dto.latest = request.queryParams("latest");
        dto.loggedInUser = getSessionUserId();
        dto.flash = getSessionFlash(request);

        return TimelineService.publicTimeline(dto);
    }

    private static Object loginGet(Request request, Response response) {
        HashMap<String, Object> context = new HashMap<>();
        context.put(FLASH, getSessionFlash(request));
        return Presentation.renderTemplate(LOGIN_HTML, context);
    }

    private static Object logout(Request request, Response response) {
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect("/public");
        return "";
    }

    private static Object followUser(Request request, Response response) {
        var dto = new FollowOrUnfollowDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = getSessionUserId();

        var params = getFromBody(request, USERNAME);
        dto.profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(USR_NAME);

        UserService.followOrUnfollow(dto, FollowerRepository::followUser, "You are now following ");
        return "";
    }

    private static Object unfollowUser(Request request, Response response) {
        var dto = new FollowOrUnfollowDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = getSessionUserId();

        var params = getFromBody(request, USERNAME);
        dto.profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(USR_NAME);

        UserService.followOrUnfollow(dto, FollowerRepository::unfollowUser, "You are no longer following ");
        return "";
    }

    private static Object userTimeline(Request request, Response response) {
        var dto = new MessagesPerUserDTO();
        dto.latest = request.queryParams("latest");
        dto.username = request.params().get(":username");
        dto.userId = getSessionUserId();
        dto.flash = getSessionFlash(request);

        return TimelineService.userTimeline(dto);
    }

    private static Object addMessage(Request request, Response response) {
        var params = getFromBody(request, USERNAME, CONTENT);

        var dto = new AddMessageDTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");
        dto.content = params.get(CONTENT);
        dto.userId = getSessionUserId();

        MessageService.addMessage(dto);
        return "";
    }

    private static Object postFollow(Request request, Response response) {
        var dto = new PostFollowDTO();
        dto.username = getParam(":username", request).get();
        dto.follow = getParam("follow", request);
        dto.unfollow = getParam("unfollow", request);

        return UserService.postFollow(dto);
    }

    private static Object login(Request request, Response response) {
        var dto = new LoginDTO();
        dto.latest = request.queryParams("latest");
        dto.userId = getSessionUserId();

        var params = getFromBody(request, USERNAME, PASSWORD);
        dto.username = params.get(USERNAME);
        dto.password = params.get(PASSWORD);

        return UserService.login(dto);
    }

    private static Object register(Request request, Response response) {
        var dto = new RegisterDTO();
        dto.latest = request.queryParams("latest");
        dto.authorization = request.headers("Authorization");
        dto.userId = getSessionUserId();

        var params = getFromBody(request, USERNAME, EMAIL, PASSWORD, "password2");
        dto.username = params.get(USERNAME);
        dto.email = params.get(EMAIL).replace("%40", "@");
        dto.password1 = params.get(PASSWORD);
        dto.password2 = params.get("password2");
        dto.pwd = params.get("pwd");

        return UserService.register(dto);
    }

    private static void registerHooks() {
        Spark.before((request, response) -> {
            Session.setSession(request, response);

            MaintenanceService.processRequest();
            //LogService.logRequest(request, Endpoints.class);

            if (request.requestMethod().equals("GET")) return;

            Integer userId = Requests.getSessionUserId();
            if (userId != null) {
                var user = UserRepository.getUserById(userId);
                if (user.isSuccess()) {
                    request.session().attribute(MessageService.USER_ID, user.get().id);
                }
            }
        });

        Spark.after(((request, response) -> {
            Session.clearSessionRequest();
        }));

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
