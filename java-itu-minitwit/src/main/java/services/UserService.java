package services;

import model.dto.*;
import model.User;
import repository.FollowerRepository;
import repository.UserRepository;
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import utilities.JSON;
import utilities.Responses;
import view.Presentation;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static services.MessageService.*;
import static services.MetricsService.updateLatest;
import static utilities.Requests.*;
import static spark.Spark.halt;
import static utilities.Responses.notFromSimulatorResponse;
import static utilities.Responses.return404;

public class UserService {


    public static int latest = 147371;

    private UserService() {}

    /*
    Adds the current user as follower of the given user.
     */
    public static void followUser(FollowOrUnfollowDTO dto) {
        followOrUnfollow(dto, FollowerRepository::followUser, "You are now following ");
    }

    /*
    Removes the current user as follower of the given user.
     */
    public static void unfollowUser(FollowOrUnfollowDTO dto) {
        followOrUnfollow(dto, FollowerRepository::unfollowUser, "You are no longer following ");
    }

    public static Result<String> validateUserCredentials(String username, String email, String password1, String password2) {
        String error;
        if (username == null || username.equals("")) {
            error = "You have to enter a username";
        } else if (email == null || !email.contains("@")) {
            error = "You have to enter a valid email address";
        } else if (password1 == null || password1.equals("")) {
            error = "You have to enter a password";
        } else if (!password1.equals(password2)) {
            error = "The two passwords do not match";
        } else if (UserRepository.getUserId(username).isSuccess()) {
            error = "The username is already taken";
        } else {
            return new Success<>("OK");
        }

        return new Failure<>(error);
    }

    public static Object login(LoginDTO dto) {
        updateLatest(dto.latest);

        if (isUserLoggedIn(dto.userId)) {
            dto.response.redirect("/");
            return "";
        }

        var loginResult = UserRepository.queryLogin(dto.username, dto.password);

        if (loginResult.isSuccess()) {
            dto.request.session().attribute(USER_ID, UserRepository.getUserId(dto.username).get());
            dto.request.session().attribute(FLASH, "You were logged in");
            dto.response.redirect("/");
            return "";
        } else {
            Failure<Boolean> error = (Failure<Boolean>) loginResult;
            HashMap<String, Object> context = new HashMap<>();
            context.put(ERROR, error.getException().getMessage());
            return Presentation.renderTemplate(LOGIN_HTML, context);
        }
    }

    private static void followOrUnfollow(FollowOrUnfollowDTO dto, BiFunction<Integer, String, Result<String>> query, String flashMessage){
        updateLatest(dto.latest);

        if (!isUserLoggedIn(dto.userId)) {
            halt(401, "You need to sign in to unfollow a user");
        }

        var rs = query.apply(getSessionUserId(dto.request), dto.profileUsername);
        if (rs.isSuccess()) {
            dto.request.session().attribute(FLASH, flashMessage + dto.profileUsername);
        }
        else {
            halt(404, rs.toString());
        }
        dto.response.redirect("/" + dto.profileUsername);
    }

    private static Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult, Response response){
        if (!UserRepository.getUserId(user).isSuccess()) {
            return return404(response);
        }
        var result = query.apply(userIdResult.get(), user);
        response.status(result.isSuccess()? HttpStatus.NO_CONTENT_204 : HttpStatus.CONFLICT_409);
        return "";
    }

    public static Object getFollow(MessagesPerUserDTO dto) {
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return notFromSimulatorResponse(dto.response);
        }

        var userIdResult = UserRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) {
            dto.response.status(HttpStatus.NOT_FOUND_404);
            dto.response.type(JSON.APPLICATION_JSON);
            return Responses.respond404();
        }
        List<User> following = FollowerRepository.getFollowing(userIdResult.get()).get();

        dto.response.status(HttpStatus.OK_200);
        dto.response.type(JSON.APPLICATION_JSON);
        return Responses.respondFollow(new JSONArray(following.stream().map(User::getUsername)));
    }

    public static Object postFollow(PostFollowDTO dto) {
        updateLatest(dto.latest);

        if (!isFromSimulator(dto.authorization)) {
            return notFromSimulatorResponse(dto.response);
        }

        var userIdResult = UserRepository.getUserId(dto.username);

        if (!userIdResult.isSuccess()) return404(dto.response);

        if (dto.follow.isSuccess()) {
            return followOrUnfollow(dto.follow.get(), FollowerRepository::followUser, userIdResult, dto.response);
        } else if (dto.unfollow.isSuccess()) {
            return followOrUnfollow(dto.unfollow.get(), FollowerRepository::unfollowUser, userIdResult, dto.response);
        } else {
            dto.response.status(HttpStatus.BAD_REQUEST_400);
            return "";
        }

    }

    public static Object register(RegisterDTO dto) {
        updateLatest(dto.latest);

        boolean isFromSimulator = isFromSimulator(dto.authorization);

        if (isFromSimulator && dto.password1 == null && dto.password2 == null) {
            dto.password1 = dto.pwd;
            dto.password2 = dto.password1;
        }

        if (isUserLoggedIn(dto.userId)) {
            return Presentation.renderTemplate(TIMELINE_HTML);
        }

        var isValid = validateUserCredentials(dto.username, dto.email, dto.password1, dto.password2);

        if (!isValid.isSuccess()) {
            if (isFromSimulator) {
                dto.response.status(HttpStatus.BAD_REQUEST_400);
                dto.response.type(JSON.APPLICATION_JSON);
                return Responses.respond404Message(isValid.getFailureMessage());
            } else {
                HashMap<String, Object> context = new HashMap<>();
                context.put(ERROR, isValid.getFailureMessage());
                context.put(USERNAME, dto.username);
                context.put(EMAIL, dto.email);
                return Presentation.renderTemplate(REGISTER_HTML, context);
            }
        }

        UserRepository.addUser(dto.username, dto.email, dto.password1);

        if (isFromSimulator) {
            dto.response.status(HttpStatus.NO_CONTENT_204);
        } else {
            dto.request.session().attribute(FLASH, "You were successfully registered and can login now");
            dto.response.redirect("/login");
        }

        return "";
    }
}
