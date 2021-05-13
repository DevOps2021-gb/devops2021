package services;

import model.dto.*;
import model.User;
import repository.IFollowerRepository;
import repository.IUserRepository;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import utilities.IRequests;
import utilities.IResponses;
import utilities.JSONFormatter;
import view.IPresentationController;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static services.MessageService.*;
import static spark.Spark.halt;

public class UserService implements IUserService {

    public static int latest = 147371;

    private final IFollowerRepository followerRepository;
    private final IUserRepository userRepository;
    private final IPresentationController presentationController;
    private final IResponses responses;
    private final IRequests requests;
    private final IMetricsService metricsService;

    public UserService(
            IFollowerRepository followerRepository,
            IUserRepository userRepository,
            IPresentationController presentationController,
            IResponses responses,
            IRequests requests,
            IMetricsService metricsService) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
        this.presentationController = presentationController;
        this.responses = responses;
        this.requests = requests;
        this.metricsService = metricsService;
    }

    public Result<String> validateUserCredentials(String username, String email, String password1, String password2) {
        String error;
        if (username == null || username.equals("")) {
            error = "You have to enter a username";
        } else if (email == null || !email.contains("@")) {
            error = "You have to enter a valid email address";
        } else if (password1 == null || password1.equals("")) {
            error = "You have to enter a password";
        } else if (!password1.equals(password2)) {
            error = "The two passwords do not match";
        } else if (userRepository.getUserId(username).isSuccess()) {
            error = "The username is already taken";
        } else {
            return new Success<>("OK");
        }

        return new Failure<>(error);
    }

    public Object login(LoginDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (requests.isUserLoggedIn(dto.userId)) {
            presentationController.redirect("/");
            return "";
        }

        var loginResult = userRepository.queryLogin(dto.username, dto.password);

        if (loginResult.isSuccess()) {
            requests.putAttribute(USER_ID, userRepository.getUserId(dto.username).get());
            requests.putAttribute(FLASH, "You were logged in");
            presentationController.redirect("/");
            return "";
        } else {
            Failure<Boolean> error = (Failure<Boolean>) loginResult;
            HashMap<String, Object> context = new HashMap<>();
            context.put(ERROR, error.getException().getMessage());
            return presentationController.renderTemplate(LOGIN_HTML, context);
        }
    }

    public void followOrUnfollow(FollowOrUnfollowDTO dto, BiFunction<Integer, String, Result<String>> query, String flashMessage){
        metricsService.updateLatest(dto.latest);

        if (!requests.isUserLoggedIn(dto.userId)) {
            halt(401, "You need to sign in to unfollow a user");
        }

        var rs = query.apply(requests.getSessionUserId(), dto.profileUsername);
        if (rs.isSuccess()) {
            requests.putAttribute(FLASH, flashMessage + dto.profileUsername);
        }
        else {
            halt(404, rs.toString());
        }
        presentationController.redirect("/" + dto.profileUsername);
    }

    private Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult){
        if (!userRepository.getUserId(user).isSuccess()) {
            return responses.return404();
        }
        var result = query.apply(userIdResult.get(), user);
        var status = result.isSuccess()? HttpStatus.NO_CONTENT_204 : HttpStatus.CONFLICT_409;
        responses.setStatus(status);
        return "";
    }

    public Object getFollow(MessagesPerUserDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (!requests.isFromSimulator(dto.authorization)) {
            return responses.notFromSimulatorResponse();
        }

        var userIdResult = userRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) {
            responses.setStatus(HttpStatus.NOT_FOUND_404);
            responses.setType(JSONFormatter.APPLICATION_JSON);
            return responses.respond404();
        }
        List<User> following = followerRepository.getFollowing(userIdResult.get()).get();

        responses.setStatus(HttpStatus.OK_200);
        responses.setType(JSONFormatter.APPLICATION_JSON);
        return responses.respondFollow(new JSONArray(following.stream().map(User::getUsername)));
    }

    public Object postFollow(PostFollowDTO dto) {
        metricsService.updateLatest(dto.latest);

        if (!requests.isFromSimulator(dto.authorization)) {
            return responses.notFromSimulatorResponse();
        }

        var userIdResult = userRepository.getUserId(dto.username);


        if (!userIdResult.isSuccess()) responses.return404();

        if (dto.follow.isSuccess()) {
            return followOrUnfollow(dto.follow.get(), followerRepository::followUser, userIdResult);
        } else if (dto.unfollow.isSuccess()) {
            return followOrUnfollow(dto.unfollow.get(), followerRepository::unfollowUser, userIdResult);
        } else {
            responses.setStatus(HttpStatus.BAD_REQUEST_400);
            return "";
        }

    }

    public Object register(RegisterDTO dto) {
        metricsService.updateLatest(dto.latest);

        boolean isFromSimulator = requests.isFromSimulator(dto.authorization);

        if (isFromSimulator && dto.password1 == null && dto.password2 == null) {
            dto.password1 = dto.pwd;
            dto.password2 = dto.password1;
        }

        if (requests.isUserLoggedIn(dto.userId)) {
            return presentationController.renderTemplate(TIMELINE_HTML);
        }

        var isValid = validateUserCredentials(dto.username, dto.email, dto.password1, dto.password2);

        if (!isValid.isSuccess()) {
            if (isFromSimulator) {
                responses.setStatus(HttpStatus.BAD_REQUEST_400);
                responses.setType(JSONFormatter.APPLICATION_JSON);
                return responses.respond404Message(isValid.getFailureMessage());
            } else {
                HashMap<String, Object> context = new HashMap<>();
                context.put(ERROR, isValid.getFailureMessage());
                context.put(USERNAME, dto.username);
                context.put(EMAIL, dto.email);
                return presentationController.renderTemplate(REGISTER_HTML, context);
            }
        }

        userRepository.addUser(dto.username, dto.email, dto.password1);

        if (isFromSimulator) {
            responses.setStatus(HttpStatus.NO_CONTENT_204);
        } else {
            requests.putAttribute(FLASH, "You were successfully registered and can login now");
            presentationController.redirect("/login");
        }

        return "";
    }
}
