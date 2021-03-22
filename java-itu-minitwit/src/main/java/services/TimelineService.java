package services;

import errorhandling.Result;
import model.Tweet;
import model.User;
import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;
import view.Presentation;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static services.MessageService.*;
import static services.MetricsService.updateLatest;
import static utilities.Requests.*;

public class TimelineService {

    private TimelineService() {}
    /*
    Displays the latest messages of all users.
    */
    public static Object publicTimeline(Request request) {
        updateLatest(request);
        var loggedInUser = getSessionUserId(request);

        HashMap<String, Object> context = new HashMap<>();
        var rsTweets = MessageRepository.publicTimeline();
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        context.put(ENDPOINT, "publicTimeline");
        context.put(TITLE, "Public Timeline");
        if(loggedInUser != null) {
            var user = UserRepository.getUserById(loggedInUser);
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        } else {
            context.put(FLASH, getSessionFlash(request));
        }

        return Presentation.renderTemplate(TIMELINE_HTML, context);
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
        HashMap<String, Object> context = new HashMap<>();
        var user = UserRepository.getUserById(getSessionUserId(request));
        if (user.isSuccess()) {
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        }
        context.put(ENDPOINT,"timeline");
        var rsTweets = MessageRepository.getPersonalTweetsById(user.get().id);
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        context.put(TITLE, "My Timeline");
        context.put(FLASH, getSessionFlash(request));
        return Presentation.renderTemplate(TIMELINE_HTML, context);
    }

    /*
Display's a users tweets.
*/
    public static Object userTimeline(Request request) {
        updateLatest(request);

        var username = getParamFromRequest(":username", request).get();

        //TODO handle this
        if (username.equals("favicon.ico")) return "";

        HashMap<String, Object> context = new HashMap<>();
        context.put(ENDPOINT, "userTimeline");

        var profileUser = UserRepository.getUser(username);
        addUserToContext(context, profileUser);

        var rsTweets = MessageRepository.getTweetsByUsername(username);
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        if (isUserLoggedIn(request)) {
            var userId = getSessionUserId(request);
            var loggedInUser = UserRepository.getUserById(userId);
            context.put(USERNAME, loggedInUser.get().getUsername());
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, userId);
            var rsIsFollowing = FollowerRepository.isFollowing(loggedInUser.get().id, profileUser.get().id);
            if(rsIsFollowing.isSuccess()) {
                context.put("followed", rsIsFollowing.get());
            }
            context.put(FLASH, getSessionFlash(request));
        } else {
            context.put(USERNAME, username);
        }
        return Presentation.renderTemplate(TIMELINE_HTML, context);
    }
    private static void addUserToContext(HashMap<String, Object> context, Result<User> profileUser) {
        if(profileUser.isSuccess()){
            context.put(TITLE, profileUser.get().getUsername() + "'s Timeline");
            context.put("profileUserId", profileUser.get().id);
            context.put("profileUserUsername", profileUser.get().getUsername());
        }
        else{
            context.put(TITLE, "404 not found" + "'s Timeline");
            context.put("profileUserId", -1);
            context.put("profileUserUsername", "404");
        }
    }
    private static void addListOfTweetsToContext(HashMap<String, Object> context, String key, Result<List<Tweet>> rsTweets) {
        if (rsTweets.isSuccess()) {
            context.put(key, rsTweets.get());
        }
        else {
            context.put(key, new ArrayList<>());
        }
    }
}
