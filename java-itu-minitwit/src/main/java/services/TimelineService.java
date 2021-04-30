package services;

import errorhandling.Result;
import model.Tweet;
import model.User;
import model.dto.MessagesPerUserDTO;
import model.dto.PublicTimelineDTO;
import model.dto.TimelineDTO;
import repository.*;
import utilities.IRequests;
import view.IPresentationController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static services.MessageService.*;

public class TimelineService implements ITimelineService{

    private final IFollowerRepository followerRepository;
    private final IMessageRepository messageRepository;
    private final IUserRepository userRepository;
    private final IPresentationController presentationController;
    private final IRequests requests;
    private final IMetricsService metricsService;

    public TimelineService(
            IFollowerRepository _followerRepository,
            IMessageRepository _messageRepository,
            IUserRepository _userRepository,
            IPresentationController _presentationController,
            IRequests _requests,
            IMetricsService _metricsService) {
        followerRepository = _followerRepository;
        messageRepository = _messageRepository;
        userRepository = _userRepository;
        presentationController = _presentationController;
        requests = _requests;
        metricsService = _metricsService;
    }

    /*
    Displays the latest messages of all users.
    */
    public Object publicTimeline(PublicTimelineDTO dto) {
        metricsService.updateLatest(dto.latest);

        HashMap<String, Object> context = new HashMap<>();
        var rsTweets = messageRepository.publicTimeline();
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        context.put(ENDPOINT, "publicTimeline");
        context.put(TITLE, "Public Timeline");

        if(dto.loggedInUser != null) {
            var user = userRepository.getUserById(dto.loggedInUser);
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        } else {
            context.put(FLASH, dto.flash);
        }

        return presentationController.renderTemplate(TIMELINE_HTML, context);
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    public Object timeline(TimelineDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (dto.userId == null) {
            presentationController.redirect("/public");
            return "";
        }
        HashMap<String, Object> context = new HashMap<>();
        var user = userRepository.getUserById(dto.userId);
        if (user.isSuccess()) {
            context.put(USERNAME, user.get().getUsername());
            context.put(USER, user.get().getUsername());
        }
        context.put(ENDPOINT,"timeline");
        var rsTweets = messageRepository.getPersonalTweetsById(user.get().id);
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        context.put(TITLE, "My Timeline");
        context.put(FLASH, dto.flash);
        return presentationController.renderTemplate(TIMELINE_HTML, context);
    }

    /*
Display's a users tweets.
*/
    public Object userTimeline(MessagesPerUserDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (dto.username.equals("favicon.ico")) return "";

        HashMap<String, Object> context = new HashMap<>();
        context.put(ENDPOINT, "userTimeline");

        var profileUser = userRepository.getUser(dto.username);
        addUserToContext(context, profileUser);

        var rsTweets = messageRepository.getTweetsByUsername(dto.username);
        addListOfTweetsToContext(context, MESSAGES, rsTweets);
        if (requests.isUserLoggedIn(dto.userId)) {
            var loggedInUser = userRepository.getUserById(dto.userId);
            context.put(USERNAME, loggedInUser.get().getUsername());
            context.put(USER, loggedInUser.get().id);
            context.put(USER_ID, dto.userId);
            var rsIsFollowing = followerRepository.isFollowing(loggedInUser.get().id, profileUser.get().id);
            if(rsIsFollowing.isSuccess()) {
                context.put("followed", rsIsFollowing.get());
            }
            context.put(FLASH, dto.flash);
        } else {
            context.put(USERNAME, dto.username);
        }
        return presentationController.renderTemplate(TIMELINE_HTML, context);
    }

    private void addUserToContext(HashMap<String, Object> context, Result<User> profileUser) {
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

    private void addListOfTweetsToContext(HashMap<String, Object> context, String key, Result<List<Tweet>> rsTweets) {
        if (rsTweets.isSuccess()) {
            context.put(key, rsTweets.get());
        }
        else {
            context.put(key, new ArrayList<>());
        }
    }
}
