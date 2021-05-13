package controllers;

import io.prometheus.client.exporter.common.TextFormat;
import model.dto.*;
import repository.IFollowerRepository;
import repository.IUserRepository;
import services.*;
import utilities.*;
import view.IPresentationController;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;

import static services.MessageService.*;
import static services.MessageService.PASSWORD;

public class Endpoints {

    private final IMessageService messageService;
    private final IUserService userService;
    private final ITimelineService timelineService;
    private final IFollowerRepository followerRepository;
    private final IUserRepository userRepository;
    private final IMaintenanceService maintenanceService;
    private final IPresentationController presentationController;
    private final IResponses responses;
    private final ILogService logService;
    private final IRequests requests;
    private final IMetricsService metricsService;

    public Endpoints(
            IMessageService messageService,
            IUserService userService,
            ITimelineService timelineService,
            IFollowerRepository followerRepository,
            IMaintenanceService maintenanceService,
            IUserRepository userRepository,
            IPresentationController presentationController,
            IResponses responses,
            ILogService logService,
            IRequests requests,
            IMetricsService metricsService) {
        this.messageService = messageService;
        this.userService = userService;
        this.timelineService = timelineService;
        this.followerRepository = followerRepository;
        this.maintenanceService = maintenanceService;
        this.userRepository = userRepository;
        this.presentationController = presentationController;
        this.responses = responses;
        this.logService = logService;
        this.requests = requests;
        this.metricsService = metricsService;
    }

    private static final String USR_NAME = ":username";
    private static final String LATEST = "latest";
    private static final String AUTHORIZATION = "Authorization";

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

    public void register() {
        registerEndpoints();
        registerHooks();
    }

    private void registerEndpoints(){
        var postEndpoints = new String[] {MSGS_USERNAME, FLLWS_USERNAME, ADD_MESSAGE, LOGIN, REGISTER};
        var getEndpoints  = new String[] {LATESTS, MESSAGES, MSGS_USERNAME, FLLWS_USERNAME, TIMELINE, METRICS, PUBLIC_TIMELINE, LOGIN, REGISTER, LOGOUT, FOLLOW, UNFOLLOW, USER_TIMELINE};
        MaintenanceService.setEndpointsToLog(getEndpoints, postEndpoints);

        Spark.post(MSGS_USERNAME,             (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), MSGS_USERNAME, this::addMessage));
        Spark.post(FLLWS_USERNAME,            (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), FLLWS_USERNAME, this::postFollow));
        Spark.post(ADD_MESSAGE,               (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), ADD_MESSAGE, this::addMessage));
        Spark.post(LOGIN,                     (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), LOGIN, this::login));
        Spark.post(REGISTER,                  (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, false), REGISTER, this::register));

        Spark.get(LATESTS,                    (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LATESTS, this::getLatest));
        Spark.get(MESSAGES,                   (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), MESSAGES, this::messages));
        Spark.get(MSGS_USERNAME,              (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), MSGS_USERNAME, this::messagesPerUser));
        Spark.get(FLLWS_USERNAME,             (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), FLLWS_USERNAME, this::getFollow));
        Spark.get(TIMELINE,                   (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), TIMELINE, this::timeline));
        Spark.get(METRICS,                    (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), METRICS, this::metrics));
        Spark.get(PUBLIC_TIMELINE,            (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), PUBLIC_TIMELINE, this::publicTimeline));
        Spark.get(LOGIN,                      (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LOGIN, this::loginGet));
        Spark.get(REGISTER,                   (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), REGISTER, (req2, res2) -> presentationController.renderTemplate(MessageService.REGISTER_HTML)));
        Spark.get(LOGOUT,                     (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), LOGOUT, this::logout));
        Spark.get(FOLLOW,                     (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), FOLLOW, this::followUser));
        Spark.get(UNFOLLOW,                   (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), UNFOLLOW, this::unfollowUser));
        Spark.get(USER_TIMELINE,              (req, res)-> maintenanceService.benchMarkEndpoint(new ResReqSparkWrapper(req, res, true), USER_TIMELINE, this::userTimeline));
    }

    private Object getLatest(Request request, Response response) {
        response.type(JSONFormatter.APPLICATION_JSON);
        return responses.respondLatest();
    }

    private Object messages(Request request, Response response) {
        var dto = new DTO();
        dto.latest = request.queryParams(LATEST);
        dto.authorization = request.headers(AUTHORIZATION);

        return messageService.getMessages(dto);
    }

    private Object messagesPerUser(Request request, Response response) {
        var dto = MessagesPerUserDTO.fromRequest(request, requests);
        return messageService.messagesPerUser(dto);
    }

    private Object getFollow(Request request, Response response) {
        var dto = MessagesPerUserDTO.fromRequest(request, requests);
        return userService.getFollow(dto);
    }

    private Object timeline(Request request, Response response) {
        var dto = new TimelineDTO();
        dto.latest = request.queryParams(LATEST);
        dto.userId = requests.getSessionUserId();
        dto.flash = requests.getSessionFlash(request);

        if (!requests.isUserLoggedIn(dto.userId)) {
            return publicTimeline(request, response);
        }

        return timelineService.timeline(dto);
    }

    private Object metrics(Request request, Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        return metricsService.metrics();
    }

    private Object publicTimeline(Request request, Response response) {
        var dto = new PublicTimelineDTO();
        dto.latest = request.queryParams(LATEST);
        dto.loggedInUser = requests.getSessionUserId();
        dto.flash = requests.getSessionFlash(request);

        return timelineService.publicTimeline(dto);
    }

    private Object loginGet(Request request, Response response) {
        HashMap<String, Object> context = new HashMap<>();
        context.put(FLASH, requests.getSessionFlash(request));
        return presentationController.renderTemplate(LOGIN_HTML, context);
    }

    private Object logout(Request request, Response response) {
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect(PUBLIC_TIMELINE);
        return "";
    }

    private Object followUser(Request request, Response response) {
        var dto = FollowOrUnfollowDTO.fromRequest(request, requests);
        userService.followOrUnfollow(dto, followerRepository::followUser, "You are now following ");
        return "";
    }

    private Object unfollowUser(Request request, Response response) {
        var dto = FollowOrUnfollowDTO.fromRequest(request, requests);
        userService.followOrUnfollow(dto, followerRepository::unfollowUser, "You are no longer following ");
        return "";
    }

    private Object userTimeline(Request request, Response response) {
        var dto = new MessagesPerUserDTO();
        dto.latest = request.queryParams(LATEST);
        dto.username = request.params().get(USR_NAME);
        dto.userId = requests.getSessionUserId();
        dto.flash = requests.getSessionFlash(request);

        return timelineService.userTimeline(dto);
    }

    private Object addMessage(Request request, Response response) {
        var params = requests.getFromBody(request, USERNAME, CONTENT).get();

        var dto = new AddMessageDTO();
        dto.latest = request.queryParams(LATEST);
        dto.authorization = request.headers(AUTHORIZATION);
        dto.username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(USR_NAME);
        dto.content = params.get(CONTENT);
        dto.userId = requests.getSessionUserId();

        messageService.addMessage(dto);
        return "";
    }

    private Object postFollow(Request request, Response response) {
        var dto = new PostFollowDTO();
        dto.username = requests.getParam(USR_NAME, request).get();
        dto.follow = requests.getParam("follow", request);
        dto.unfollow = requests.getParam("unfollow", request);
        dto.authorization = request.headers(AUTHORIZATION);

        return userService.postFollow(dto);
    }

    private Object login(Request request, Response response) {
        var dto = new LoginDTO();
        dto.latest = request.queryParams(LATEST);
        dto.userId = requests.getSessionUserId();
        dto.authorization = request.headers(AUTHORIZATION);

        var params = requests.getFromBody(request, USERNAME, PASSWORD).get();
        dto.username = params.get(USERNAME);
        dto.password = params.get(PASSWORD);

        return userService.login(dto);
    }

    private Object register(Request request, Response response) {
        var dto = new RegisterDTO();
        dto.latest = request.queryParams(LATEST);
        dto.authorization = request.headers(AUTHORIZATION);
        dto.userId = requests.getSessionUserId();

        var params = requests.getFromBody(request, USERNAME, EMAIL, PASSWORD, "password2").get();
        dto.username = params.get(USERNAME);
        dto.email = params.get(EMAIL).replace("%40", "@");
        dto.password1 = params.get(PASSWORD);
        dto.password2 = params.get("password2");
        dto.pwd = params.get("pwd");

        return userService.register(dto);
    }

    private void registerHooks() {
        Spark.before((request, response) -> {
            Session.setSession(request, response);

            maintenanceService.processRequest();
            logService.logRequest(request, Endpoints.class);

            if (request.requestMethod().equals("GET")) return;

            Integer userId = requests.getSessionUserId();
            if (userId != null) {
                var user = userRepository.getUserById(userId);
                if (user.isSuccess()) {
                    request.session().attribute(MessageService.USER_ID, user.get().id);
                }
            }
        });

        Spark.notFound((request, response) -> {
            response.type(JSONFormatter.APPLICATION_JSON);
            return responses.respond404();
        });

        Spark.internalServerError((request, response) -> {
            response.type(JSONFormatter.APPLICATION_JSON);
            return responses.respond500();
        });
    }
}
