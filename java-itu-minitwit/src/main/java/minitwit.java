import RoP.Failure;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import spark.Request;
import spark.Response;

import java.util.HashMap;

import static spark.Spark.*;

public class minitwit {

    //configuration
    static Boolean DEBUG        = true;

    public static void main(String[] args) {
        try {
            staticFiles.location("/");

            registerHooks();

            registerEndpoints();

            //Queries.initDb();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerHooks() {
        before((request, response) -> {
            /*
            Make sure we are connected to the database each request and look
            up the current user so that we know he's there.
             */

            logRequest(request);

            Integer userId = getSessionUserId(request);
            if (userId == null) {
                return;
            }

            var user = Queries.querySingleUser("select * from user where userId = ?", userId.toString());
            if (user.isSuccess()) {
                request.session().attribute("userId", user.get().userId());
            }
        });

        after((request, response) -> {
            /*
            Closes the database again at the end of the request.
            */
            //TODO decide how to handle before/after with connection
        });

        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"404 not found\"}";
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"500 server error\"}";
        });
    }

    private static void logRequest(Request request) {
        //TODO figure out why this happens
        if (request.url().contains("favicon.ico")) return;

        if (request.params().size() == 0) {
            System.out.println(request.requestMethod() + " " + request.url());
        } else {
            System.out.println(request.requestMethod() + " " + request.url() + " with args " + request.params());
        }
    }

    private static void registerEndpoints() {
        get("/",                    minitwit::timeline);
        get("/public",              minitwit::publicTimeline);
        post("/add_message",        minitwit::addMessage);
        post("/login",              minitwit::login);
        get("/login",               (req, res)-> renderTemplate("login.html"));
        get("/register",            (req, res)-> renderTemplate("register.html"));
        post("/register",           minitwit::register);
        get("/logout",              minitwit::logout);
        get("/:username/follow",    minitwit::followUser);
        get("/:username/unfollow",  minitwit::unfollowUser);
        get("/:username",           minitwit::userTimeline);
    }

    private static Boolean userLoggedIn(Request request) {
        return getSessionUserId(request) != null;
    }

    private static Object renderTemplate(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), Charsets.UTF_8), context);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    private static Object renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline(Request request, Response response) {
        System.out.println("We got a visitor from: " + request.ip());

        if (!userLoggedIn(request)) {
            return publicTimeline(request, response);
        }

        if (getSessionUserId(request) == null) {
            response.redirect("/public");
            return null;
        } else {
            var user = Queries.getUserById(getSessionUserId(request)).get();

            return renderTemplate("timeline.html", new HashMap<>() {{
                put("username", user.username());
                put("user", user.username());
                put("endpoint","timeline");
                put("messages", Queries.getPersonalTweetsById(user.userId()).get());
                put("title", "My Timeline");
            }});
        }
    }
    /*
     Displays the latest messages of all users.
    */
    public static Object publicTimeline(Request request, Response response) {
        var loggedInUser = getSessionUserId(request);
        if(loggedInUser != null) {
            var user = Queries.getUserById(loggedInUser);
            return renderTemplate("timeline.html", new HashMap<>() {{
                put("messages", Queries.publicTimeline().get());
                put("username", user.get().username());
                put("user", user.get().username());
                put("endpoint", "public_timeline");
                put("title", "Public Timeline");
            }});
        } else {
            return renderTemplate("timeline.html", new HashMap<>() {
                {
                    put("messages", Queries.publicTimeline().get());
                    put("endpoint", "public_timeline");
                    put("title", "Public Timeline");
                }});
        }
    }

    /*
    Display's a users tweets.
     */
    static Object userTimeline(Request request, Response response) {
        var profileUsername = request.params(":username");

        //TODO figure out how to avoid this hack
        if (profileUsername.equals("favicon.ico")) return "";

        if (!userLoggedIn(request)) {
            var profileUser = Queries.getUser(profileUsername);

            return renderTemplate("timeline.html", new HashMap<>() {{
                put("endpoint", "user_timeline");
                put("username", profileUsername);
                put("title", profileUser.get().username() + "'s Timeline");
                put("profile_user_id", profileUser.get().userId());
                put("profile_user_username", profileUser.get().username());
                put("messages", Queries.getTweetsByUsername(profileUsername).get());
            }});
        } else {

            var userId = getSessionUserId(request);

            var profileUser = Queries.getUser(profileUsername);
            var loggedInUser = Queries.getUserById(userId);

            return renderTemplate("timeline.html", new HashMap<>() {{
                put("endpoint", "userTimeline");
                put("username", loggedInUser.get().username());
                put("title", profileUser.get().username() + "'s Timeline");
                put("user", loggedInUser.get().userId());
                put("user_id", userId);
                put("profile_user_id", profileUser.get().userId());
                put("profile_user_username", profileUser.get().username());
                put("followed", Queries.following(loggedInUser.get().userId(), profileUser.get().userId()).get());
                put("messages", Queries.getTweetsByUsername(profileUsername).get());
            }});
        }
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object followUser(Request request, Response response) {
        String profileUsername = request.params("username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to follow a user");
            return null;
        }

        var rs = Queries.followUser(getSessionUserId(request),profileUsername);
        if (rs.isSuccess()) {
            //flash
            System.out.println("You are now following " + profileUsername);
        }
        else {
            System.out.println(rs.toString());
            halt(404, rs.toString());
        }
        response.redirect("/" + profileUsername);
        return null;
    }

    private static Integer getSessionUserId(Request request) {
        return request.session().attribute("user_id");
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollowUser(Request request, Response response) {
        String profileUsername = request.params("username");

        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to unfollow a user");
            return null;
        }

        var rs = Queries.unfollowUser(getSessionUserId(request), profileUsername);
        if (rs.isSuccess()) {
            //flash
            System.out.println("You are no longer following " + profileUsername);
        }
        else {
            System.out.println(rs.toString());
            halt(404, rs.toString());
        }
        response.redirect("/" + profileUsername);
        return null;
    }

    /*
    Registers a new message for the user.
     */
    static Object addMessage(Request request, Response response) {
        if (!userLoggedIn(request)) {
            halt(401, "You need to sign in to post a message");
            return null;
        }

        var rs = Queries.addMessage(request.queryParams("text"), getSessionUserId(request));
        if (rs.isSuccess()){
            System.out.println("Your message was recorded");
            //flash('Your message was recorded')
            response.redirect("/" + request.params("username"));
        } else {
            response.redirect("/");
        }
        return null;
    }

    /*
    Logs the user in.
     */
    static Object login(Request request, Response response) {
        String username = request.queryParams("username");
        String password = request.queryParams("password");

        if (userLoggedIn(request)) {
            response.redirect("/");
            return null;
        }

        var loginResult = Queries.queryLogin(username, password);

        if (loginResult.isSuccess()) {
            request.session().attribute("user_id", Queries.getUserId(username).get());

            response.redirect("/");
            return null;
        } else {
            System.out.println(loginResult);

            return renderTemplate("login.html", new HashMap<>() {{
                put("error", loginResult);
            }});
        }
    }

    /*
    Registers the user.
     */
    static Object register(Request request, Response response) {
        String username = request.queryParams("username");
        String email = request.queryParams("email");
        String password1 = request.queryParams("password");
        String password2 = request.queryParams("password2");

        if (userLoggedIn(request)) {
            return renderTemplate("timeline.html");
        }

        var result = Queries.register(username, email, password1, password2);

        if (result.isSuccess()) {
            response.redirect("/login");
            return null;
        } else {
            Failure<String> error = (Failure<String>) result;

            return renderTemplate("register.html", new HashMap<>() {{
                put("error", error.getException().getMessage());
                //TODO handle each case individually
                put("username", username);
                put("email", email);
            }});
        }
    }

    /*
    Logs the user out
     */
    static Object logout(Request request, Response response) {
        System.out.println("You were logged out");
        request.session().removeAttribute("user_id");
        response.redirect("/login");
        return null;
    }

}
