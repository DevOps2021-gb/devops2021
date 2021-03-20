package Logic;

import Model.Tweet;
import Model.User;
import Persistence.FollowerRepository;
import Persistence.MessageRepository;
import Persistence.UserRepository;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import Utilities.Formatting;
import Utilities.Hashing;
import Utilities.JSON;
import View.Presentation;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
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
    public static final String USER = "user";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String ENDPOINT = "endpoint";
    private static final String MESSAGES = "messages";
    private static final String TITLE = "title";
    public static final String CONTENT = "content";

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

    public static Object messages(Request request, Response response) {
        updateLatest(request);
        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }
        var tweets = MessageRepository.publicTimeline().get();
        return JSON.tweetsToJSONResponse(tweets, response);
    }

    public static Object messagesPerUser(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var username = getParamFromRequest(":username", request).get();
        var userIdResult = UserRepository.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        } else {
            var tweets = MessageRepository.getTweetsByUsername(username).get();
            return JSON.tweetsToJSONResponse(tweets, response);
        }
    }

    public static Object getFollow(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var username = getParamFromRequest(":username", request).get();
        var userIdResult = UserRepository.getUserId(username);

        if (!userIdResult.isSuccess()) {
            response.status(HttpStatus.NOT_FOUND_404);
            response.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        }
        List<User> following = FollowerRepository.getFollowing(userIdResult.get()).get();

        response.status(HttpStatus.OK_200);
        response.type(JSON.APPLICATION_JSON);
        return JSON.respondFollow(new JSONArray(following.stream().map(User::getUsername)));
    }

    private static Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult, Response response){
        if (!UserRepository.getUserId(user).isSuccess()) {
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

        var username = getParamFromRequest(":username", request).get();
        var userIdResult = UserRepository.getUserId(username);

        if (!userIdResult.isSuccess()) {
            return return404(response);
        }

        var follow = getParamFromRequest("follow", request);
        var unfollow = getParamFromRequest("unfollow", request);

        if (follow.isSuccess()) {
            return followOrUnfollow(follow.get(), FollowerRepository::followUser, userIdResult, response);
        } else if (unfollow.isSuccess()) {
            return followOrUnfollow(unfollow.get(), FollowerRepository::unfollowUser, userIdResult, response);
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
        var user = UserRepository.getUserById(getSessionUserId(request)).get();
        HashMap<String, Object> context = new HashMap<>();
        context.put(USERNAME, user.getUsername());
        context.put(USER, user.getUsername());
        context.put(ENDPOINT,"timeline");
        context.put(MESSAGES, MessageRepository.getPersonalTweetsById(user.id).get());
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
        context.put(MESSAGES, MessageRepository.publicTimeline().get());
        context.put(ENDPOINT, "publicTimeline");
        context.put(TITLE, "Public Timeline");
        if(loggedInUser != null) {
            var user = UserRepository.getUserById(loggedInUser);
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

        var username = getParamFromRequest(":username", request).get();

        //TODO handle this
        if (username.equals("favicon.ico")) return "";

        var profileUser = UserRepository.getUser(username);

        HashMap<String, Object> context = new HashMap<>();
        context.put(ENDPOINT, "userTimeline");
        context.put(TITLE, profileUser.get().getUsername() + "'s Timeline");
        context.put("profileUserId", profileUser.get().id);
        context.put("profileUserUsername", profileUser.get().getUsername());
        context.put(MESSAGES, MessageRepository.getTweetsByUsername(username).get());

        if (isUserLoggedIn(request)) {
            var userId = getSessionUserId(request);
            var loggedInUser = UserRepository.getUserById(userId);
            context.put(USERNAME, loggedInUser.get().getUsername());
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, userId);
            context.put("followed", FollowerRepository.isFollowing(loggedInUser.get().id, profileUser.get().id).get());
            context.put(FLASH, getSessionFlash(request));
        } else {
            context.put(USERNAME, username);
        }
        return Presentation.renderTemplate(TIMELINE_HTML, context);
    }

    private static void followOrUnfollow(Request request, Response response, BiFunction<Integer, String, Result<String>> query, String flashMessage){
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
    public static void followUser(Request request, Response response) {
        followOrUnfollow(request, response, FollowerRepository::followUser, "You are now following ");
    }

    /*
    Removes the current user as follower of the given user.
     */
    public static void unfollowUser(Request request, Response response) {
        followOrUnfollow(request, response, FollowerRepository::unfollowUser, "You are no longer following ");
    }

    /*
    Registers a new message for the user.
     */
    public static void addMessage(Request request, Response response) {
        updateLatest(request);

        var params = getParamsFromRequest(request, USERNAME, CONTENT);
        String username = params.get(USERNAME) != null ? params.get(USERNAME) : params.get(":username");
        String content = params.get(CONTENT);

        Integer userId;
        if(username == null){
            if (!isUserLoggedIn(request)) {
                halt(401, "You need to sign in to post a message");
            }
            userId = getSessionUserId(request);
        }
        else {
            userId = UserRepository.getUserId(username).get();
        }

        var rs = MessageRepository.addMessage(content, userId);
        if (rs.isSuccess()){
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
            } else {
                System.out.println("Your message was recorded");
                request.session().attribute(FLASH, "Your message was recorded");
            }
        }
        response.redirect("/");
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

        var loginResult = UserRepository.queryLogin(username, password);

        if (loginResult.isSuccess()) {
            request.session().attribute(USER_ID, UserRepository.getUserId(username).get());
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

        var isValid = validateUserCredentials(username, email, password1, password2);

        if (!isValid.isSuccess()) {
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                response.type(JSON.APPLICATION_JSON);
                return JSON.respond404Message(isValid.getFailureMessage());
            } else {
                HashMap<String, Object> context = new HashMap<>();
                context.put(ERROR, isValid.getFailureMessage());
                context.put(USERNAME, username);
                context.put(EMAIL, email);
                return Presentation.renderTemplate(REGISTER_HTML, context);
            }
        }

        UserRepository.AddUser(username, email, password1);

        if (isRequestFromSimulator(request)) {
            response.status(HttpStatus.NO_CONTENT_204);
        } else {
            request.session().attribute(FLASH, "You were successfully registered and can login now");
            response.redirect("/login");
        }

        return null;

    }

    public static Result<String> validateUserCredentials(String username, String email, String password1, String password2) {
        if (username == null || username.equals("")) {
            return new Failure<>("You have to enter a username");
        } else if (email == null || !email.contains("@")) {
            return new Failure<>("You have to enter a valid email address");
        } else if (password1 == null || password1.equals("")) {
            return new Failure<>("You have to enter a password");
        } else if (!password1.equals(password2)) {
            return new Failure<>("The two passwords do not match");
        } else if (UserRepository.getUserId(username).isSuccess()) {
            return new Failure<>("The username is already taken");
        } else {
            return new Success<>("OK");
        }
    }

    public static void logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect("/public");
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
