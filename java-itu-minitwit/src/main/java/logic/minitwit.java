package logic;

import model.Tweet;
import model.User;
import rop.Failure;
import rop.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

import static spark.Spark.*;
import static spark.Spark.get;

public class minitwit {
    private static int latest = 147371;

    // templates
    private static final String TIMELINE_HTML = "timeline.html";
    private static final String REGISTER_HTML = "register.html";
    private static final String LOGIN_HTML = "login.html";

    // context fields
    private static final String FLASH = "flash";
    private static final String ERROR = "error";
    private static final String USER_ID = "userId";
    private static final String USER = "user";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String ENDPOINT = "endpoint";
    private static final String MESSAGES = "messages";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    // responses
    private static final String JSON = "application/json";
    private static final String MESSAGE404 = "{\"message\":\"404 not found\"}";

    //endpoints
    private static String[] entryPointsGetOrder     = new String[]{"/latest", "/msgs", "/msgs/:username", "/fllws/:username", "/", "/metrics", "/public", "/login", "/register", "/logout", "/:username/follow", "/:username/unfollow","/:username"};
    private static String[] entryPointsPostOrder    = new String[]{"/msgs/:username","/fllws/:username","/add_message","/login","/register"};

    private static Map<String, BiFunction<Request, Response, Object>> endpointsGet = new HashMap<>();

    private static Map<String, BiFunction<Request, Response, Object>> endpointsPost =new HashMap<>();

    public static void main(String[] args) {
        try {
            staticFiles.location("/");

            if(args.length > 0) {
                DB.setCONNECTIONSTRING(args[0]);
                DB.setUSER(args[1]);
                DB.setPW(args[2]);
            }

            registerHooks();

            registerEndpoints();

            //add db clear here if working LOCALLY

            Logger.startSchedules();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerHooks() {
        before((request, response) -> {
            Logger.processRequest();
            /*
            Make sure we are connected to the database each request and look
            up the current user so that we know he's there.
             */

            logRequest(request);

            Integer userId = getSessionUserId(request);
            if (userId == null) {
                return;
            }

            var user = Queries.getUserById(userId);
            if (user.isSuccess()) {
                request.session().attribute(USER_ID, user.get().id);
            }
        });

        after((request, response) -> {
            /*
            Closes the database again at the end of the request.
            */
            //TODO decide how to handle before/after with connection
        });

        notFound((req, res) -> {
            res.type(JSON);
            return MESSAGE404;
        });

        internalServerError((req, res) -> {
            res.type(JSON);
            return "{\"message\":\"500 server error\"}";
        });
    }

    private static void logRequest(Request request) {
        if (request.url().contains("favicon.ico")) return;

        if (request.params().size() == 0) {
            System.out.println(request.requestMethod() + " " + request.url());
        } else {
            System.out.println(request.requestMethod() + " " + request.url() + " with args " + request.params());
        }
    }
    private static void setUpEntryPointsMap(){
        endpointsGet.put("/latest",              minitwit::getLatest);
        endpointsGet.put("/msgs",                minitwit::messages);
        endpointsGet.put("/msgs/:username",      minitwit::messagesPerUser);
        endpointsGet.put("/fllws/:username",     minitwit::getFollow);
        endpointsGet.put("/",                    minitwit::timeline);
        endpointsGet.put("/metrics",             minitwit::metrics);
        endpointsGet.put("/public",              minitwit::publicTimeline);
        endpointsGet.put("/login",               minitwit::loginGet);
        endpointsGet.put("/register",            (req, res)-> renderTemplate(REGISTER_HTML));
        endpointsGet.put("/logout",              minitwit::logout);
        endpointsGet.put("/:username/follow",    minitwit::followUser);
        endpointsGet.put("/:username/unfollow",  minitwit::unfollowUser);
        endpointsGet.put("/:username",           minitwit::userTimeline);

        endpointsPost.put("/msgs/:username",      minitwit::addMessage);
        endpointsPost.put("/fllws/:username",     minitwit::postFollow);
        endpointsPost.put("/add_message",         minitwit::addMessage);
        endpointsPost.put("/login",               minitwit::login);
        endpointsPost.put("/register",            minitwit::register);

    }
    private static void registerEndpoints() {
        setUpEntryPointsMap();
        for(String point : entryPointsGetOrder) {
            get(point, (req, res)-> Logger.benchMarkEndpoint(point, endpointsGet.get(point), req, res));
        }
        for(String point : entryPointsPostOrder) {
            post(point, (req, res)-> Logger.benchMarkEndpoint(point, endpointsPost.get(point), req, res));
        }
        Logger.setEndpointsToLog(entryPointsGetOrder, entryPointsPostOrder);
    }

    private static boolean reqFromSimulator(Request request) {
        var fromSimulator = request.headers("Authorization");
        return fromSimulator != null && fromSimulator.equals("Basic c2ltdWxhdG9yOnN1cGVyX3NhZmUh");
    }

    private static Object notFromSimulatorResponse(Response response) {
        response.status(HttpStatus.FORBIDDEN_403);
        response.type(JSON);
        var error = "You are not authorized to use this resource!";
        return "{\"status\": 403, \"error_msg\": " + error + " }";
    }

    private static void updateLatest(Request request) {
        String stringLat = request.queryParams("latest");
        if (stringLat != null) {
            try {
                latest = Integer.parseInt(stringLat);
            } catch (NumberFormatException ne) {
                // Do nothing
            }
        }
    }

    private static Boolean userLoggedIn(Request request) {
        return getSessionUserId(request) != null;
    }

    private static Integer getSessionUserId(Request request) {
        return request.session().attribute(USER_ID);
    }

    private static Object getSessionFlash(Request request) {
        var msg = request.session().attribute(FLASH);
        request.session().removeAttribute(FLASH);
        return msg;
    }

    private static Object renderTemplate(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), StandardCharsets.UTF_8), context);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    private static Object renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }

    private static Map<String,String> getParamsFromRequest(Request request, String ... args){
        Map<String,String> map = new HashMap<>();

        map.putAll(request.params());

        for (String arg : args) {
            if (request.queryParams(arg) != null) {
                map.put(arg, request.queryParams(arg));
            }
        }

        if (!request.body().isEmpty()) {
            if(request.body().startsWith("{")) { // is JSON
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, String> temp = mapper.readValue(request.body(), Map.class);
                    map.putAll(temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                for(String keyValue : request.body().split(" *& *")) {
                    String[] pairs = keyValue.split(" *= *", 2);
                    map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
                }
            }
        }
        return map;
    }

    private static Object getLatest(Request request, Response response) {
        response.type(JSON);
        return "{\"latest\":" + latest + "}";
    }

    private static Object tweetsToJSONResponse(List<Tweet> tweets, Response response) {
        List<JSONObject> msgs = new ArrayList<>();
        for (Tweet t : tweets) {
            HashMap<String, String> msg = new HashMap<>();
            msg.put(CONTENT, t.getText());
            msg.put("pub_date", t.getPubDate());
            msg.put(USER, t.getUsername());
            msgs.add(new JSONObject(msg));
        }
        var json = new JSONArray(msgs);
        if (json.length() == 0) {
            response.status(HttpStatus.NO_CONTENT_204);
            return "";
        } else {
            response.status(HttpStatus.OK_200);
            response.type(JSON);
            return json;
        }
    }

    private static Object messages(Request request, Response response) {
        updateLatest(request);
        if (!reqFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }
        var tweets = Queries.publicTimeline().get();
        return tweetsToJSONResponse(tweets, response);
    }

    private static Object messagesPerUser(Request request, Response response) {
        updateLatest(request);

        if (!reqFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Queries.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON);
            return MESSAGE404;
        } else {
            var tweets = Queries.getTweetsByUsername(username).get();
            return tweetsToJSONResponse(tweets, response);
        }
    }

    private static Object getFollow(Request request, Response response) {
        updateLatest(request);

        if (!reqFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Queries.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON);
            return MESSAGE404;
        }

        List<User> following = Queries.getFollowing(userIdResult.get()).get();
        JSONArray json = new JSONArray(following.stream().map(User::getUsername));

        response.status(HttpStatus.OK_200);
        response.type(JSON);
        return "{\"follows\": " + json + " }";
    }

    private static String return404(Response response){
        response.status(HttpStatus.NOT_FOUND_404);
        response.type(JSON);
        return MESSAGE404;
    }
    private static Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult, Response response){
        if (!Queries.getUserId(user).isSuccess()) {
            return return404(response);
        }
        var result = query.apply(userIdResult.get(), user);
        response.status(result.isSuccess()? HttpStatus.NO_CONTENT_204 : HttpStatus.CONFLICT_409);
        return "";
    }

    private static Object postFollow(Request request, Response response) {
        updateLatest(request);

        if (!reqFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Queries.getUserId(username);

        if (!userIdResult.isSuccess()) {
            return return404(response);
        }

        if (params.containsKey("follow")) {
            return followOrUnfollow(params.get("follow"), Queries::followUser, userIdResult, response);
        } else if (params.containsKey("unfollow")) {
            return followOrUnfollow(params.get("unfollow"), Queries::unfollowUser, userIdResult, response);
        }
        response.status(HttpStatus.BAD_REQUEST_400);
        return "";
    }


    private static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;
    private static Object metrics(Request request, Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        final StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, registry.metricFamilySamples());
        } catch (IOException e) {}
        return writer.toString();
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline(Request request, Response response) {
        updateLatest(request);

        if (!userLoggedIn(request)) {
            return publicTimeline(request, response);
        }

        if (getSessionUserId(request) == null) {
            response.redirect("/public");
            return null;
        }
        var user = Queries.getUserById(getSessionUserId(request)).get();
        HashMap<String, Object> context = new HashMap<>();
        context.put(USERNAME, user.getUsername());
        context.put(USER, user.getUsername());
        context.put(ENDPOINT,"timeline");
        context.put(MESSAGES, Queries.getPersonalTweetsById(user.id).get());
        context.put(TITLE, "My Timeline");
        context.put(FLASH, getSessionFlash(request));
        return renderTemplate(TIMELINE_HTML, context);
    }

    /*
     Displays the latest messages of all users.
    */
    public static Object publicTimeline(Request request, Response response) {
        updateLatest(request);
        var loggedInUser = getSessionUserId(request);
        Object returnPage;
        HashMap<String, Object> context = new HashMap<>();
        context.put(MESSAGES, Queries.publicTimeline().get());
        context.put(ENDPOINT, "publicTimeline");
        context.put(TITLE, "Public Timeline");
        if(loggedInUser != null) {
            var user = Queries.getUserById(loggedInUser);
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        } else {
            context.put(FLASH, getSessionFlash(request));
        }
        returnPage = renderTemplate(TIMELINE_HTML, context);
        return returnPage;
    }

    /*
    Display's a users tweets.
     */
    static Object userTimeline(Request request, Response response) {
        updateLatest(request);
        var params = getParamsFromRequest(request);
        var profileUsername = params.get(":username");

        //TODO figure out how to avoid this hack
        if (profileUsername.equals("favicon.ico")) return "";
        HashMap<String, Object> context = new HashMap<>();
        context.put(ENDPOINT, "userTimeline");
        var profileUser = Queries.getUser(profileUsername);
        context.put(TITLE, profileUser.get().getUsername() + "'s Timeline");
        context.put("profileUserId", profileUser.get().id);
        context.put("profileUserUsername", profileUser.get().getUsername());
        context.put(MESSAGES, Queries.getTweetsByUsername(profileUsername).get());
        if (!userLoggedIn(request)) {
            context.put(USERNAME, profileUsername);
        } else {
            var userId = getSessionUserId(request);
            var loggedInUser = Queries.getUserById(userId);
            context.put(USERNAME, loggedInUser.get().getUsername());
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, userId);
            context.put("followed", Queries.isFollowing(loggedInUser.get().id, profileUser.get().id).get());
            context.put(FLASH, getSessionFlash(request));
        }
        return renderTemplate(TIMELINE_HTML, context);
    }

    static Object followOrUnfollow(Request request, Response response, BiFunction<Integer, String, Result<String>> query, String flashMessage){
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME);
        String profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to unfollow a user");
            return null;
        }

        var rs = query.apply(getSessionUserId(request), profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute(FLASH, flashMessage + profileUsername);
        }
        else {
            halt(404, rs.toString());
        }
        response.redirect("/" + profileUsername);
        return null;
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object followUser(Request request, Response response) {
        followOrUnfollow(request, response, Queries::followUser, "You are now following ");
        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollowUser(Request request, Response response) {
        followOrUnfollow(request, response, Queries::unfollowUser, "You are no longer following ");
        return null;
    }

    /*
    Registers a new message for the user.
     */
    static Object addMessage(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, CONTENT);
        String username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");
        String content = params.get(CONTENT);

        Integer userId;
        if(username == null){
            if (!userLoggedIn(request)) {
                halt(401, "You need to sign in to post a message");
                return null;
            }
            userId = getSessionUserId(request);
        }
        else {
            userId = Queries.getUserId(username).get();
        }

        var rs = Queries.addMessage(content, userId);
        if (rs.isSuccess()){
            if (reqFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                System.out.println("Your message was recorded");
                request.session().attribute(FLASH, "Your message was recorded");
            }
        }
        response.redirect("/");
        return null;
    }

    /*
    Logs the user in.
     */
    static Object login(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, PASSWORD);
        String username = params.get(USERNAME);
        String password = params.get(PASSWORD);

        if (userLoggedIn(request)) {
            response.redirect("/");
            return null;
        }

        var loginResult = Queries.queryLogin(username, password);

        if (loginResult.isSuccess()) {
            request.session().attribute(USER_ID, Queries.getUserId(username).get());
            request.session().attribute(FLASH, "You were logged in");
            response.redirect("/");
            return null;
        } else {
            Failure<Boolean> error = (Failure<Boolean>) loginResult;
            HashMap<String, Object> context = new HashMap<>();
            context.put(ERROR, error.getException().getMessage());
            return renderTemplate(LOGIN_HTML, context);
        }
    }

    /*
    Get endpoint for login, needed to show flash message
     */
    static Object loginGet(Request request, Response response) {
        HashMap<String, Object> context = new HashMap<>();
        context.put(FLASH, getSessionFlash(request));
        return renderTemplate(LOGIN_HTML, context);
    }

    /*
    Registers the user.
     */
    static Object register(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, EMAIL, PASSWORD, "password2");
        String username = params.get(USERNAME);
        String email = params.get(EMAIL).replace("%40", "@");
        String password1 = params.get(PASSWORD);
        String password2 = params.get("password2");
        if (reqFromSimulator(request) && password1 == null && password2 == null) {
            password1 = params.get("pwd");
            password2 = password1;
        }

        if (userLoggedIn(request)) {
            return renderTemplate(TIMELINE_HTML);
        }

        var result = Queries.register(username, email, password1, password2);

        if (result.isSuccess()) {
            if (reqFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                request.session().attribute(FLASH, "You were successfully registered and can login now");
                response.redirect("/login");
                return null;
            }
        } else {
            if (reqFromSimulator(request)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                response.type(JSON);
                return "{\"message\":\"404 not found\", \"error_msg\": "+ result.getFailureMessage() + "}";
            } else {
                HashMap<String, Object> context = new HashMap<>();
                context.put(ERROR, result.getFailureMessage());
                context.put(USERNAME, username);
                context.put(EMAIL, email);
                return renderTemplate(REGISTER_HTML, context);
            }
        }
    }

    /*
    Logs the user out
     */
    static Object logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect("/public");
        return null;
    }
}
