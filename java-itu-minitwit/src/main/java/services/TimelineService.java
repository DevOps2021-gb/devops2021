package services;

import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;
import view.Presentation;
import spark.Request;
import spark.Response;

import java.util.HashMap;

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
}
