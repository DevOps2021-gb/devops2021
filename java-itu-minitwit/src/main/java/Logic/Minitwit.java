package Logic;

import Controller.Endpoints;
import Model.Tweet;
import Model.User;
import Persistence.DB;
import Persistence.Repositories;
import RoP.Failure;
import RoP.Result;
import Utilities.Formatting;
import Utilities.Hashing;
import Utilities.JSON;
import View.Presentation;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiFunction;

import static Utilities.JSON.return404;
import static Utilities.Requests.*;
import static spark.Spark.*;

public class Minitwit {
    private static int latest = 147371;

    // templates
    private static final String TIMELINE_HTML = "timeline.html";
    public static final String REGISTER_HTML = "register.html";
    private static final String LOGIN_HTML = "login.html";

    // context fields
    public static final String FLASH = "flash";
    private static final String ERROR = "error";
    public static final String USER_ID = "userId";
    private static final String USER = "user";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String ENDPOINT = "endpoint";
    private static final String MESSAGES = "messages";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";

    private static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    private static void updateLatest(Request request) {
        String requestLatest = request.queryParams("latest");
        if (requestLatest != null) {
            try {
                latest = Integer.parseInt(requestLatest);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getLatest(Response response) {
        response.type(JSON.APPLICATION_JSON);
        return JSON.respondLatest(latest);
    }

    private static Object tweetsToJSONResponse(List<Tweet> tweets, Response response) {
        List<JSONObject> messages = new ArrayList<>();
        for (Tweet t : tweets) {
            HashMap<String, String> msg = new HashMap<>();
            msg.put(CONTENT, t.getText());
            msg.put("pub_date", t.getPubDate());
            msg.put(USER, t.getUsername());
            messages.add(new JSONObject(msg));
        }
        var json = new JSONArray(messages);
        if (json.length() == 0) {
            response.status(HttpStatus.NO_CONTENT_204);
            return "";
        } else {
            response.status(HttpStatus.OK_200);
            response.type(JSON.APPLICATION_JSON);
            return json;
        }
    }

    public static Object messages(Request request, Response response) {
        updateLatest(request);
        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }
        var tweets = Repositories.publicTimeline().get();
        return tweetsToJSONResponse(tweets, response);
    }

    public static Object messagesPerUser(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Repositories.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        } else {
            var tweets = Repositories.getTweetsByUsername(username).get();
            return tweetsToJSONResponse(tweets, response);
        }
    }

    public static Object getFollow(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Repositories.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        }
        List<User> following = Repositories.getFollowing(userIdResult.get()).get();

        response.status(HttpStatus.OK_200);
        response.type(JSON.APPLICATION_JSON);
        return JSON.respondFollow(new JSONArray(following.stream().map(User::getUsername)));
    }

    private static Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult, Response response){
        if (!Repositories.getUserId(user).isSuccess()) {
            return return404(response);
        }
        var result = query.apply(userIdResult.get(), user);
        response.status(result.isSuccess()? HttpStatus.NO_CONTENT_204 : HttpStatus.CONFLICT_409);
        return "";
    }

    public static Object postFollow(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var params = getParamsFromRequest(request);
        var username = params.get(":username");
        var userIdResult = Repositories.getUserId(username);

        if (!userIdResult.isSuccess()) {
            return return404(response);
        }

        if (params.containsKey("follow")) {
            return followOrUnfollow(params.get("follow"), Repositories::followUser, userIdResult, response);
        } else if (params.containsKey("unfollow")) {
            return followOrUnfollow(params.get("unfollow"), Repositories::unfollowUser, userIdResult, response);
        }
        response.status(HttpStatus.BAD_REQUEST_400);
        return "";
    }

    public static Object metrics(Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        final StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, registry.metricFamilySamples());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    public static Object timeline(Request request, Response response) {
        updateLatest(request);

        if (!isUserLoggedIn(request)) {
            return publicTimeline(request);
        }

        if (getSessionUserId(request) == null) {
            response.redirect("/public");
            return null;
        }
        var user = Repositories.getUserById(getSessionUserId(request)).get();
        HashMap<String, Object> context = new HashMap<>();
        context.put(USERNAME, user.getUsername());
        context.put(USER, user.getUsername());
        context.put(ENDPOINT,"timeline");
        context.put(MESSAGES, Repositories.getPersonalTweetsById(user.id).get());
        context.put(TITLE, "My Timeline");
        context.put(FLASH, getSessionFlash(request));
        return Presentation.renderTemplate(TIMELINE_HTML, context);
    }

    /*
     Displays the latest messages of all users.
    */
    public static Object publicTimeline(Request request) {
        updateLatest(request);
        var loggedInUser = getSessionUserId(request);
        Object returnPage;
        HashMap<String, Object> context = new HashMap<>();
        context.put(MESSAGES, Repositories.publicTimeline().get());
        context.put(ENDPOINT, "publicTimeline");
        context.put(TITLE, "Public Timeline");
        if(loggedInUser != null) {
            var user = Repositories.getUserById(loggedInUser);
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        } else {
            context.put(FLASH, getSessionFlash(request));
        }
        returnPage = Presentation.renderTemplate(TIMELINE_HTML, context);
        return returnPage;
    }

    /*
    Display's a users tweets.
     */
    public static Object userTimeline(Request request) {
        updateLatest(request);
        var params = getParamsFromRequest(request);
        var profileUsername = params.get(":username");

        //TODO handle this
        if (profileUsername.equals("favicon.ico")) return "";

        HashMap<String, Object> context = new HashMap<>();
        context.put(ENDPOINT, "userTimeline");
        var profileUser = Repositories.getUser(profileUsername);
        context.put(TITLE, profileUser.get().getUsername() + "'s Timeline");
        context.put("profileUserId", profileUser.get().id);
        context.put("profileUserUsername", profileUser.get().getUsername());
        context.put(MESSAGES, Repositories.getTweetsByUsername(profileUsername).get());
        if (!isUserLoggedIn(request)) {
            context.put(USERNAME, profileUsername);
        } else {
            var userId = getSessionUserId(request);
            var loggedInUser = Repositories.getUserById(userId);
            context.put(USERNAME, loggedInUser.get().getUsername());
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, userId);
            context.put("followed", Repositories.isFollowing(loggedInUser.get().id, profileUser.get().id).get());
            context.put(FLASH, getSessionFlash(request));
        }
        return Presentation.renderTemplate(TIMELINE_HTML, context);
    }

    static void followOrUnfollow(Request request, Response response, BiFunction<Integer, String, Result<String>> query, String flashMessage){
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME);
        String profileUsername = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");

        if (!isUserLoggedIn(request)) {
            halt(401, "You need to sign in to unfollow a user");
        }

        var rs = query.apply(getSessionUserId(request), profileUsername);
        if (rs.isSuccess()) {
            request.session().attribute(FLASH, flashMessage + profileUsername);
        }
        else {
            halt(404, rs.toString());
        }
        response.redirect("/" + profileUsername);
    }

    /*
    Adds the current user as follower of the given user.
     */
    public static Object followUser(Request request, Response response) {
        followOrUnfollow(request, response, Repositories::followUser, "You are now following ");
        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    public static Object unfollowUser(Request request, Response response) {
        followOrUnfollow(request, response, Repositories::unfollowUser, "You are no longer following ");
        return null;
    }

    /*
    Registers a new message for the user.
     */
    public static Object addMessage(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, CONTENT);
        String username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");
        String content = params.get(CONTENT);

        Integer userId;
        if(username == null){
            if (!isUserLoggedIn(request)) {
                halt(401, "You need to sign in to post a message");
                return null;
            }
            userId = getSessionUserId(request);
        }
        else {
            userId = Repositories.getUserId(username).get();
        }

        var rs = Repositories.addMessage(content, userId);
        if (rs.isSuccess()){
            if (isRequestFromSimulator(request)) {
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

    public static Object login(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, PASSWORD);
        String username = params.get(USERNAME);
        String password = params.get(PASSWORD);

        if (isUserLoggedIn(request)) {
            response.redirect("/");
            return null;
        }

        var loginResult = Repositories.queryLogin(username, password);

        if (loginResult.isSuccess()) {
            request.session().attribute(USER_ID, Repositories.getUserId(username).get());
            request.session().attribute(FLASH, "You were logged in");
            response.redirect("/");
            return null;
        } else {
            Failure<Boolean> error = (Failure<Boolean>) loginResult;
            HashMap<String, Object> context = new HashMap<>();
            context.put(ERROR, error.getException().getMessage());
            return Presentation.renderTemplate(LOGIN_HTML, context);
        }
    }

    /*
    Get endpoint for login, needed to show flash message
     */
    public static Object loginGet(Request request) {
        HashMap<String, Object> context = new HashMap<>();
        context.put(FLASH, getSessionFlash(request));
        return Presentation.renderTemplate(LOGIN_HTML, context);
    }

    public static Object register(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, EMAIL, PASSWORD, "password2");
        String username = params.get(USERNAME);
        String email = params.get(EMAIL).replace("%40", "@");
        String password1 = params.get(PASSWORD);
        String password2 = params.get("password2");
        if (isRequestFromSimulator(request) && password1 == null && password2 == null) {
            password1 = params.get("pwd");
            password2 = password1;
        }

        if (isUserLoggedIn(request)) {
            return Presentation.renderTemplate(TIMELINE_HTML);
        }

        var result = validateUserCredentialsAndRegister(username, email, password1, password2);

        if (result.isSuccess()) {
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                request.session().attribute(FLASH, "You were successfully registered and can login now");
                response.redirect("/login");
                return null;
            }
        } else {
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                response.type(JSON.APPLICATION_JSON);
                return JSON.respond404Message(result.getFailureMessage());
            } else {
                HashMap<String, Object> context = new HashMap<>();
                context.put(ERROR, result.getFailureMessage());
                context.put(USERNAME, username);
                context.put(EMAIL, email);
                return Presentation.renderTemplate(REGISTER_HTML, context);
            }
        }
    }

    static Result<String> validateUserCredentialsAndRegister(String username, String email, String password1, String password2) {
        String error;
        if (username == null || username.equals("")) {
            error = "You have to enter a username";
        } else if (email == null || !email.contains("@")) {
            error = "You have to enter a valid email address";
        } else if (password1 == null || password1.equals("")) {
            error = "You have to enter a password";
        } else if (!password1.equals(password2)) {
            error = "The two passwords do not match";
        } else if (Repositories.getUserId(username).isSuccess()) {
            error = "The username is already taken";
        } else {
            //TODO remove this from this method and return OK instead
            return Repositories.InsertUser(username, email, password1);
        }
        return new Failure<>(error);
    }

    public static Object logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect("/public");
        return null;
    }

    public static List<Tweet> tweetsFromListOfHashMap(List<HashMap> result){
        List<Tweet> tweets = new ArrayList<>();
        for (HashMap hm: result) {
            String email        = (String) hm.get("email");
            String username     = (String) hm.get("username");
            String text         = (String) hm.get("text");
            String pubDate      = Formatting.formatDatetime((long) hm.get("pubDate") + "").get();
            String profilePic   = Hashing.gravatarUrl(email);
            tweets.add(new Tweet(email, username, text, pubDate, profilePic));
        }
        return tweets;
    }
}
