import Model.Tweet;
import Model.User;
import RoP.Failure;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class minitwit {
    private static int latest = 110371;

    //configuration
    static Boolean DEBUG        = true;

    public static void main(String[] args) {
        try {
            staticFiles.location("/");

            if(args.length > 0) {
                System.out.println("Connecting to remote database");
                DB.setCONNECTIONSTRING(args[0]);
                DB.setUSER(args[1]);
                DB.setPW(args[2]);
            }

            registerHooks();

            registerEndpoints();

            //add db clear here if working LOCALLY
            Queries.initDb();

            //Logger.StartLogging();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerHooks() {
        before((request, response) -> {
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
                request.session().attribute("userId", user.get().id);
            }
        });

        after((request, response) -> {
            /*
            Closes the database again at the end of the request.
            */
            //TODO decide how to handle before/after with connection
        });

        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"404 not found\"}";
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"500 server error\"}";
        });
    }

    private static void logRequest(Request request) {
        //TODO figure out why this happens
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
        get("/fllws/:username",     minitwit::getFollow); //TODO
        post("/fllws/:username",    minitwit::postFollow);

        get("/",                    minitwit::timeline);
        get("/public",              minitwit::publicTimeline);
        post("/add_message",        minitwit::addMessage);
        post("/login",              minitwit::login);
        get("/login",               (req, res)-> renderTemplate("login.html", new HashMap<>() {{ put("flash", getSessionFlash(req)); }}));
        get("/register",            (req, res)-> renderTemplate("register.html"));
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
        response.type("application/json");
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
        return request.session().attribute("userId");
    }

    private static Object getSessionFlash(Request request) {
        var msg = request.session().attribute("flash");
        request.session().removeAttribute("flash");
        return msg;
    }

    private static Object renderTemplate(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), Charsets.UTF_8), context);
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
        response.type("application/json");
        return "{\"latest\":" + latest + "}";
    }

    private static Object tweetsToJSONResponse(List<Tweet> tweets, Response response) {
        List<JSONObject> msgs = new ArrayList<>();
        for (Tweet t : tweets) {
            HashMap<String, String> msg = new HashMap<>();
            msg.put("content", t.text);
            msg.put("pub_date", t.pubDate);
            msg.put("user", t.username);
            msgs.add(new JSONObject(msg));
        }
        var json = new JSONArray(msgs);
        if (json.length() == 0) {
            response.status(HttpStatus.NO_CONTENT_204);
            return "";
        } else {
            response.status(HttpStatus.OK_200);
            response.type("application/json");
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
            response.type("application/json");
            return "{\"message\":\"404 not found\"}";
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
            response.type("application/json");
            return "{\"message\":\"404 not found\"}";
        }

        List<User> following = Queries.getFollowing(userIdResult.get()).get();
        JSONArray json = new JSONArray(following.stream().map(u->u.username));

        response.status(HttpStatus.OK_200);
        response.type("application/json");
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
            response.type("application/json");
            return "{\"message\":\"404 not found\"}";
        }

        if (params.containsKey("follow")) {
            var followUser = params.get("follow");
            if (!Queries.getUserId(followUser).isSuccess()) {
                response.status(HttpStatus.NOT_FOUND_404);
                response.type("application/json");
                return "{\"message\":\"404 not found\"}";
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
                response.type("application/json");
                return "{\"message\":\"404 not found\"}";
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

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline(Request request, Response response) {
        updateLatest(request);
        System.out.println("We got a visitor from: " + request.ip());

        if (!userLoggedIn(request)) {
            return publicTimeline(request, response);
        }

        if (getSessionUserId(request) == null) {
            response.redirect("/public");
            return null;
        }
        var user = Queries.getUserById(getSessionUserId(request)).get();

        return renderTemplate("timeline.html", new HashMap<>() {{
            put("username", user.username);
            put("user", user.username);
            put("endpoint","timeline");
            put("messages", Queries.getPersonalTweetsById(user.id).get());
            put("title", "My Timeline");
            put("flash", getSessionFlash(request));
        }});
    }

    /*
     Displays the latest messages of all users.
    */
    public static Object publicTimeline(Request request, Response response) {
        var startTime = System.nanoTime();
        updateLatest(request);
        var loggedInUser = getSessionUserId(request);
        Object returnPage;
        if(loggedInUser != null) {
            var user = Queries.getUserById(loggedInUser);
            returnPage = renderTemplate("timeline.html", new HashMap<>() {{
                put("messages", Queries.publicTimeline().get());
                put("username", user.get().username);
                put("user", user.get().username);
                put("endpoint", "publicTimeline");
                put("title", "Public Timeline");
            }});
        } else {
            returnPage = renderTemplate("timeline.html", new HashMap<>() {
                {
                    put("messages", Queries.publicTimeline().get());
                    put("endpoint", "publicTimeline");
                    put("title", "Public Timeline");
                    put("flash", getSessionFlash(request));
                }});
        }
        //Logger.LogResponseTimeFrontPage(System.nanoTime() - startTime);
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

        if (!userLoggedIn(request)) {
            var profileUser = Queries.getUser(profileUsername);
            return renderTemplate("timeline.html", new HashMap<>() {{
                put("endpoint", "userTimeline");
                put("username", profileUsername);
                put("title", profileUser.get().username + "'s Timeline");
                put("profileUserId", profileUser.get().id);
                put("profileUserUsername", profileUser.get().username);
                put("messages", Queries.getTweetsByUsername(profileUsername).get());
            }});
        } else {

            var userId = getSessionUserId(request);

            var profileUser = Queries.getUser(profileUsername);
            var loggedInUser = Queries.getUserById(userId);

            return renderTemplate("timeline.html", new HashMap<>() {{
                put("endpoint", "userTimeline");
                put("username", loggedInUser.get().username);
                put("title", profileUser.get().username + "'s Timeline");
                put("user", loggedInUser.get().id);
                put("userId", userId);
                put("profileUserId", profileUser.get().id);
                put("profileUserUsername", profileUser.get().username);
                put("followed", Queries.isFollowing(loggedInUser.get().id, profileUser.get().id).get());
                put("messages", Queries.getTweetsByUsername(profileUsername).get());
                put("flash", getSessionFlash(request));
            }});
        }
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object followUser(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, "username");
        String profileUsername = params.get("username") != null ? params.get("username") : params.get(":username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to follow a user");
            return null;
        }

        var rs = Queries.followUser(getSessionUserId(request),profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute("flash", "You are now following " + profileUsername);
            System.out.println("You are now following " + profileUsername);
        }
        else {
            System.out.println(rs.toString());
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

        var params = getParamsFromRequest(request, "username");
        String profileUsername = params.get("username") != null ? params.get("username") : params.get(":username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to unfollow a user");
            return null;
        }

        var rs = Queries.unfollowUser(getSessionUserId(request), profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute("flash", "You are no longer following " + profileUsername);
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

        var params = getParamsFromRequest(request, "username", "content");
        String username = params.get("username") != null ? params.get("username") : params.get(":username");
        String content = params.get("content");

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
                request.session().attribute("flash", "Your message was recorded");
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

        var params = getParamsFromRequest(request, "username", "password");
        String username = params.get("username");
        String password = params.get("password");

        if (userLoggedIn(request)) {
            response.redirect("/");
            return null;
        }

        var loginResult = Queries.queryLogin(username, password);

        if (loginResult.isSuccess()) {
            request.session().attribute("userId", Queries.getUserId(username).get());
            request.session().attribute("flash", "You were logged in");
            response.redirect("/");
            return null;
        } else {
            Failure<Boolean> error = (Failure<Boolean>) loginResult;
            System.out.println(loginResult);

            return renderTemplate("login.html", new HashMap<>() {{
                put("error", error.getException().getMessage());
            }});
        }
    }

    /*
    Registers the user.
     */
    static Object register(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, "username", "email", "password", "password2");
        String username = params.get("username");
        String email = params.get("email").replaceAll("%40", "@");
        String password1 = params.get("password");
        String password2 = params.get("password2");
        if (reqFromSimulator(request) && password1 == null && password2 == null) {
            password1 = params.get("pwd");
            password2 = password1;
        }

        if (userLoggedIn(request)) {
            return renderTemplate("timeline.html");
        }

        var result = Queries.register(username, email, password1, password2);

        if (result.isSuccess()) {
            if (reqFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                request.session().attribute("flash", "You were successfully registered and can login now");
                response.redirect("/login");
                return null;
            }
        } else {
            if (reqFromSimulator(request)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                response.type("application/json");
                return "{\"message\":\"404 not found\", \"error_msg\": "+ result.getFailureMessage() + "}";
            } else {
                return renderTemplate("register.html", new HashMap<>() {{
                    put("error", result.getFailureMessage());
                    put("username", username);
                    put("email", email);
                }});

            }
        }
    }

    /*
    Logs the user out
     */
    static Object logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute("userId");
        request.session().attribute("flash", "You were logged out");
        response.redirect("/public");
        return null;
    }
}
