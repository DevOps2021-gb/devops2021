package Service;

import Model.User;
import Persistence.FollowerRepository;
import Persistence.MessageRepository;
import Persistence.UserRepository;
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import Utilities.JSON;
import View.Presentation;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static Service.MessageService.*;
import static Service.MetricsService.updateLatest;
import static Utilities.JSON.return404;
import static Utilities.Requests.*;
import static spark.Spark.halt;

public class UserService {
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

    public static void logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute(USER_ID);
        request.session().attribute(FLASH, "You were logged out");
        response.redirect("/public");
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

    private static Object followOrUnfollow (String user, BiFunction<Integer, String, Result<String>> query, Result<Integer> userIdResult, Response response){
        if (!UserRepository.getUserId(user).isSuccess()) {
            return return404(response);
        }
        var result = query.apply(userIdResult.get(), user);
        response.status(result.isSuccess()? HttpStatus.NO_CONTENT_204 : HttpStatus.CONFLICT_409);
        return "";
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

    public static Object postFollow(Request request, Response response) {
        updateLatest(request);

        if (!isRequestFromSimulator(request)) {
            return notFromSimulatorResponse(response);
        }

        var username = getParamFromRequest(":username", request).get();
        var userIdResult = UserRepository.getUserId(username);

        if (userIdResult.isSuccess()) {
            var follow = getParamFromRequest("follow", request);
            var unfollow = getParamFromRequest("unfollow", request);
            BiFunction<Integer, String, Result<String>> func = null;
            String user = "";

            if (!follow.isSuccess() && !unfollow.isSuccess()) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return null;
            }

            if (follow.isSuccess()) {
                user = follow.get();
                func = FollowerRepository::followUser;
            } else if (unfollow.isSuccess()) {
                user = unfollow.get();
                func = FollowerRepository::unfollowUser;
            }

            return followOrUnfollow(user, func, userIdResult, response);
        }

        return return404(response);
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
}
