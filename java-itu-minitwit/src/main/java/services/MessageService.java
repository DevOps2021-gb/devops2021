package services;

import model.Tweet;
import persistence.MessageRepository;
import persistence.UserRepository;
import utilities.Formatting;
import utilities.Hashing;
import utilities.JSON;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static services.MetricsService.updateLatest;
import static utilities.Requests.*;
import static spark.Spark.halt;

public class MessageService {

    private MessageService() {}

    // templates
    public static final String TIMELINE_HTML = "timeline.html";
    public static final String REGISTER_HTML = "register.html";
    public static final String LOGIN_HTML = "login.html";

    // context fields
    public static final String FLASH    = "flash";
    public static final String ERROR    = "error";
    public static final String USER_ID  = "userId";
    public static final String USER     = "user";
    public static final String USERNAME = "username";
    public static final String EMAIL    = "email";
    public static final String PASSWORD = "password";
    public static final String ENDPOINT = "endpoint";
    public static final String MESSAGES = "messages";
    public static final String TITLE    = "title";
    public static final String CONTENT  = "content";

    public static Object getLatest(Response response) {
        response.type(JSON.APPLICATION_JSON);
        return JSON.respondLatest(UserService.latest);
    }

    public static Object getMessages(Request request, Response response) {
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
            return JSON.respond404();
        } else {
            var tweets = MessageRepository.getTweetsByUsername(username).get();
            return JSON.tweetsToJSONResponse(tweets, response);
        }
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
            var userIdRes = UserRepository.getUserId(username);

            if (!userIdRes.isSuccess()) {
                response.status(HttpStatus.NO_CONTENT_204);
                return;
            }

            userId = userIdRes.get();
        }

        var rs = MessageRepository.addMessage(content, userId);
        if (rs.isSuccess()){
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.NO_CONTENT_204);
            } else {
                var message = "Your message was recorded";
                LogService.log(MessageService.class, message);
                request.session().attribute(FLASH, message);
                response.redirect("/");
            }
        } else {
            if (isRequestFromSimulator(request)) {
                response.status(HttpStatus.FORBIDDEN_403);
            } else {
                response.redirect("/");
            }
        }

    }

    public static List<Tweet> tweetsFromListOfHashMap(List<HashMap> result){
        List<Tweet> tweets = new ArrayList<>();
        for (HashMap hm: result) {
            String email        = (String) hm.get(EMAIL);
            String username     = (String) hm.get(USERNAME);
            String text         = (String) hm.get("text");
            String pubDate      = Formatting.formatDatetime((long) hm.get("pubDate") + "").get();
            String profilePic   = Hashing.getGravatarUrl(email);
            tweets.add(new Tweet(email, username, text, pubDate, profilePic));
        }
        return tweets;
    }
}
