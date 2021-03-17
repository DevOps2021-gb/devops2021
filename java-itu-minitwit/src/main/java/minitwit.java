import Model.Tweet;
import Model.User;
import RoP.Failure;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

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

            Logger.StartSchedules();

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

    private static void registerEndpoints() {
        // Simulator endpoints
        get("/latest",              minitwit::getLatest);
        get("/msgs",                minitwit::messages);
        get("/msgs/:username",      minitwit::messagesPerUser);
        post("/msgs/:username",     minitwit::addMessage);
        get("/fllws/:username",     minitwit::getFollow);
        post("/fllws/:username",    minitwit::postFollow);

        get("/",                    minitwit::timeline);
        get("/metrics",             minitwit::metrics);
        get("/public",              minitwit::publicTimeline);
        post("/add_message",        minitwit::addMessage);
        post("/login",              minitwit::login);
        get("/login",               minitwit::loginGet);
        get("/register",            (req, res)-> renderTemplate(REGISTER_HTML));
        post("/register",           minitwit::register);
        get("/logout",              minitwit::logout);
        get("/:username/follow",    minitwit::followUser);
        get("/:username/unfollow",  minitwit::unfollowUser);
        get("/:username",           minitwit::userTimeline);
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
            msg.put(CONTENT, t.text);
            msg.put("pub_date", t.pubDate);
            msg.put(USER, t.username);
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
        JSONArray json = new JSONArray(following.stream().map(u->u.username));

        response.status(HttpStatus.OK_200);
        response.type(JSON);
        return "{\"follows\": " + json + " }";
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
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON);
            return MESSAGE404;
        }

        if (params.containsKey("follow")) {
            var followUser = params.get("follow");
            if (!Queries.getUserId(followUser).isSuccess()) {
                response.status(HttpStatus.NOT_FOUND_404);
                response.type(JSON);
                return MESSAGE404;
            }
            var result = Queries.followUser(userIdResult.get(), followUser);
            if (result.isSuccess()) {
                response.status(HttpStatus.NO_CONTENT_204);
            } else {
                response.status(HttpStatus.CONFLICT_409);
            }
            return "";
        } else if (params.containsKey("unfollow")) {
            var unfollowUser = params.get("unfollow");
            if (!Queries.getUserId(unfollowUser).isSuccess()) {
                response.status(HttpStatus.NOT_FOUND_404);
                response.type(JSON);
                return MESSAGE404;
            }
            var result = Queries.unfollowUser(userIdResult.get(), unfollowUser);
            if (result.isSuccess()) {
                response.status(HttpStatus.NO_CONTENT_204);
            } else {
                response.status(HttpStatus.CONFLICT_409);
            }
            return "";
        }
        response.status(HttpStatus.BAD_REQUEST_400);
        return "";
    }


    private static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;
    private static Object metrics(Request request, Response response) throws IOException {
        //todo test
        response.type(TextFormat.CONTENT_TYPE_004);
        final StringWriter writer = new StringWriter();
        TextFormat.write004(writer, registry.metricFamilySamples());
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
        context.put(USERNAME, user.username);
        context.put(USER, user.username);
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
        var startTime = System.currentTimeMillis();
        updateLatest(request);
        var loggedInUser = getSessionUserId(request);
        Object returnPage;
        HashMap<String, Object> context = new HashMap<>();
        if(loggedInUser != null) {
            var user = Queries.getUserById(loggedInUser);
            context.put(MESSAGES, Queries.publicTimeline().get());
            context.put(USERNAME, user.get().username);
            context.put(USER, user.get().username);
            context.put(ENDPOINT, "publicTimeline");
            context.put(TITLE, "Public Timeline");
            returnPage = renderTemplate(TIMELINE_HTML, context);
        } else {
            context.put(MESSAGES, Queries.publicTimeline().get());
            context.put(ENDPOINT, "publicTimeline");
            context.put(TITLE, "Public Timeline");
            context.put(FLASH, getSessionFlash(request));
            returnPage = renderTemplate(TIMELINE_HTML, context);
        }
        Logger.LogResponseTimeFrontPage(System.currentTimeMillis() - startTime);
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
        if (!userLoggedIn(request)) {
            var profileUser = Queries.getUser(profileUsername);
            context.put(ENDPOINT, "userTimeline");
            context.put(USERNAME, profileUsername);
            context.put(TITLE, profileUser.get().username + "'s Timeline");
            context.put("profileUserId", profileUser.get().id);
            context.put("profileUserUsername", profileUser.get().username);
            context.put(MESSAGES, Queries.getTweetsByUsername(profileUsername).get());
            return renderTemplate(TIMELINE_HTML, context);
        } else {
            var userId = getSessionUserId(request);
            var profileUser = Queries.getUser(profileUsername);
            var loggedInUser = Queries.getUserById(userId);
            context.put(ENDPOINT, "userTimeline");
            context.put(USERNAME, loggedInUser.get().username);
            context.put(TITLE, profileUser.get().username + "'s Timeline");
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, userId);
            context.put("profileUserId", profileUser.get().id);
            context.put("profileUserUsername", profileUser.get().username);
            context.put("followed", Queries.isFollowing(loggedInUser.get().id, profileUser.get().id).get());
            context.put(MESSAGES, Queries.getTweetsByUsername(profileUsername).get());
            context.put(FLASH, getSessionFlash(request));
            return renderTemplate(TIMELINE_HTML, context);
        }
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object followUser(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME);
        String profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to follow a user");
            return null;
        }

        var rs = Queries.followUser(getSessionUserId(request),profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute(FLASH, "You are now following " + profileUsername);
        }
        else {
            halt(404, rs.toString());
        }

        response.redirect("/" + profileUsername);
        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollowUser(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME);
        String profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to unfollow a user");
            return null;
        }

        var rs = Queries.unfollowUser(getSessionUserId(request), profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute(FLASH, "You are no longer following " + profileUsername);
            System.out.println("You are no longer following " + profileUsername);
        }
        else {
            System.out.println(rs.toString());
            halt(404, rs.toString());
        }
        response.redirect("/" + profileUsername);
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
