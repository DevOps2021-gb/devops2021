package services;

import model.DTO;
import model.Tweet;
import persistence.MessageRepository;
import persistence.UserRepository;
import utilities.Formatting;
import utilities.Hashing;
import utilities.JSON;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;
import utilities.Responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


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
    private static final Logger logger  = Logger.getLogger(MessageService.class.getSimpleName());

    public static Object getMessages(DTO dto) {
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return Responses.notFromSimulatorResponse(dto.response);
        }

        var tweets = MessageRepository.publicTimeline().get();
        return JSON.tweetsToJSONResponse(tweets, dto.response);
    }

    public static Object messagesPerUser(DTO dto) {
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return Responses.notFromSimulatorResponse(dto.response);
        }

        var userIdResult = UserRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) {
            dto.response.status(HttpStatus.NOT_FOUND_404);
            dto.response.type(JSON.APPLICATION_JSON);
            return Responses.respond404();
        } else {
            var tweets = MessageRepository.getTweetsByUsername(dto.username).get();
            return JSON.tweetsToJSONResponse(tweets, dto.response);
        }
    }
    /*
    Registers a new message for the user.
     */
    public static void addMessage(DTO dto) {
        updateLatest(dto.latest);
        Integer userId;
        if(dto.username == null){
            if (!isUserLoggedIn(dto.request)) {
                halt(401, "You need to sign in to post a message");
            }
            userId = getSessionUserId(dto.request);
        }
        else {
            var userIdRes = UserRepository.getUserId(dto.username);

            if (!userIdRes.isSuccess()) {
                dto.response.status(HttpStatus.NO_CONTENT_204);
                return;
            }

            userId = userIdRes.get();
        }

        var rs = MessageRepository.addMessage(dto.content, userId);
        if (rs.isSuccess()){
            if (isFromSimulator(dto.authorization)) {
                dto.response.status(HttpStatus.NO_CONTENT_204);
            } else {
                var message = "Your message was recorded";
                logger.log(Level.INFO,message);
                dto.request.session().attribute(FLASH, message);
                dto.response.redirect("/");
            }
        } else {
            if (isFromSimulator(dto.authorization)) {
                dto.response.status(HttpStatus.FORBIDDEN_403);
            } else {
                dto.response.redirect("/");
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
