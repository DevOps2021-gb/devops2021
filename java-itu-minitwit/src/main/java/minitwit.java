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

            //Queries.init_db();

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

            Integer user_id = getSessionUserId(request);
            if (user_id == null) {
                return;
            }

            var user = Queries.querySingleUser("select * from user where user_id = ?", user_id.toString());
            if (user.isSuccess()) {
                request.session().attribute("user_id", user.get().user_id());
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
        get("/public",              minitwit::public_timeline);
        post("/add_message",        minitwit::add_message);
        post("/login",              minitwit::login);
        get("/login",               (req, res)-> render_template("login.html"));
        get("/register",            (req, res)-> render_template("register.html"));
        post("/register",           minitwit::register);
        get("/logout",              minitwit::logout);
        get("/:username/follow",    minitwit::follow_user);
        get("/:username/unfollow",  minitwit::unfollow_user);
        get("/:username",           minitwit::user_timeline);
    }

    private static Boolean user_logged_in(Request request) {
        return getSessionUserId(request) != null;
    }

    private static Object render_template(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), Charsets.UTF_8), context);
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    private static Object render_template(String template) {
        return render_template(template, new HashMap<>());
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline(Request request, Response response) {
        System.out.println("We got a visitor from: " + request.ip());

        if (!user_logged_in(request)) {
            return public_timeline(request, response);
        }

        if (getSessionUserId(request) == null) {
            response.redirect("/public");
            return null;
        } else {
            var user = Queries.getUserById(getSessionUserId(request)).get();

            return render_template("timeline.html", new HashMap<>() {{
                put("username", user.username());
                put("user", user.username());
                put("endpoint","timeline");
                put("messages", Queries.getPersonalTweetsById(user.user_id()).get());
                put("title", "My Timeline");
            }});
        }
    }
    /*
     Displays the latest messages of all users.
    */
    public static Object public_timeline(Request request, Response response) {
        var logged_in_user = getSessionUserId(request);
        if(logged_in_user != null) {
            var user = Queries.getUserById(logged_in_user);
            return render_template("timeline.html", new HashMap<>() {{
                put("messages", Queries.public_timeline().get());
                put("username", user.get().username());
                put("user", user.get().username());
                put("endpoint", "public_timeline");
                put("title", "Public Timeline");
            }});
        } else {
            return render_template("timeline.html", new HashMap<>() {
                {
                    put("messages", Queries.public_timeline().get());
                    put("endpoint", "public_timeline");
                    put("title", "Public Timeline");
                }});
        }
    }

    /*
    Display's a users tweets.
     */
    static Object user_timeline(Request request, Response response) {
        var profile_username = request.params(":username");

        //TODO figure out how to avoid this hack
        if (profile_username.equals("favicon.ico")) return "";

        if (!user_logged_in(request)) {
            var profile_user = Queries.getUser(profile_username);

            return render_template("timeline.html", new HashMap<>() {{
                put("endpoint", "user_timeline");
                put("username", profile_username);
                put("title", profile_user.get().username() + "'s Timeline");
                put("profile_user_id", profile_user.get().user_id());
                put("profile_user_username", profile_user.get().username());
                put("messages", Queries.getTweetsByUsername(profile_username).get());
            }});
        } else {

            var user_id = getSessionUserId(request);

            var profile_user = Queries.getUser(profile_username);
            var logged_in_user = Queries.getUserById(user_id);

            return render_template("timeline.html", new HashMap<>() {{
                put("endpoint", "user_timeline");
                put("username", logged_in_user.get().username());
                put("title", profile_user.get().username() + "'s Timeline");
                put("user", logged_in_user.get().user_id());
                put("user_id", user_id);
                put("profile_user_id", profile_user.get().user_id());
                put("profile_user_username", profile_user.get().username());
                put("followed", Queries.following(logged_in_user.get().user_id(), profile_user.get().user_id()).get());
                put("messages", Queries.getTweetsByUsername(profile_username).get());
            }});
        }
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object follow_user(Request request, Response response) {
        String profileUsername = request.params("username");

        if (!user_logged_in(request)) {
            halt(401, "You need to sign in to follow a user");
            return null;
        }

        var rs = Queries.follow_user(getSessionUserId(request),profileUsername);
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
    static Object unfollow_user(Request request, Response response) {
        String profileUsername = request.params("username");

        if (!user_logged_in(request)) {
            halt(401, "You need to sign in to unfollow a user");
            return null;
        }

        var rs = Queries.unfollow_user(getSessionUserId(request), profileUsername);
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
    static Object add_message(Request request, Response response) {
        if (!user_logged_in(request)) {
            halt(401, "You need to sign in to post a message");
            return null;
        }

        var rs = Queries.add_message(request.queryParams("text"), getSessionUserId(request));
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

        if (user_logged_in(request)) {
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

            return render_template("login.html", new HashMap<>() {{
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

        if (user_logged_in(request)) {
            return render_template("timeline.html");
        }

        var result = Queries.register(username, email, password1, password2);

        if (result.isSuccess()) {
            response.redirect("/login");
            return null;
        } else {
            return render_template("register.html", new HashMap<>() {{
                put("error", result.getFailureMessage());
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
