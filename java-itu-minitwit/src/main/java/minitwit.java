import RoP.Failure;
import RoP.Result;
import RoP.Success;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.ibatis.jdbc.ScriptRunner;
import spark.Request;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class minitwit {

    //configuration
    static Boolean DEBUG        = true;
    static String SECRET_KEY    = "development key";

    public static void main(String[] args) {
        try {
            before((request, response) -> Queries.before_request(request));

            after((request, response) ->  Queries.after_request());

            notFound((req, res) -> {
                res.type("application/json");
                return "{\"message\":\"Custom 404\"}";
            });

            internalServerError((req, res) -> {
                res.type("application/json");
                return "{\"message\":\"Custom 500 handling\"}";
            });

            registerEndpoints();

            Queries.init_db();
            System.out.println("test if done");
            System.out.println(Queries.get_user_id("bob").get());
            System.out.println(timeline(null, "bob"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void registerEndpoints() {
        get("/",                    (req, res)-> timeline(null, null));
        get("/public",              (req, res)-> Queries.public_timeline());
        get("/:username",           (req, res)-> user_timeline(req.params("username")));
        get("/:username/follow",    (req, res)-> follow_user(null));
        get("/:username/unfollow",  (req, res)-> unfollow_user(null));
        post("/add_message",        (req, res)-> add_message(null));
        post("/login",              (req, res)-> login("POST", req.params("username"), null, null));
        get("/login",               (req, res)-> login("GET", req.params("username"), null, null));
        get("/register",            (req, res)-> register(null, null, null, null, null));
        post("/register",           (req, res)-> register(null, null, null, null, null));
        get("/logout",              (req, res)-> logout());
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
    Format a timestamp for display.
     */
    Result<String> format_datetime(String timestamp) {
        try {
            //https://stackoverflow.com/questions/18915075/java-convert-string-to-timestamp
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(timestamp);
            return new Success<>(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    /*
    Return the gravatar image for the given email address.
     */
    void gravatar_url(String email, int size) {
        //hashing boogaloo
        //return 'http://www.gravatar.com/avatar/%s?d=identicon&s=%d' % \ (md5(email.strip().lower().encode('utf-8')).hexdigest(), size)
    }
    /*
    Java does not support default arguments
     */
    void gravatar_url(String email) {
        gravatar_url(email, 80);
    }

    /*
    Shows a users timeline or if no user is logged in it will
    redirect to the public timeline.  This timeline shows the user's
    messages as well as all the messages of followed users.
     */
    static Object timeline(String remote_addr, String user_id) {
        System.out.println("We got a visitor from: " + remote_addr);
        if (!Queries.user_logged_in()) {
            return public_timeline();
        }

        var rs = Queries.timeline(user_id);
        return render_template("timeline.html");
    }
    /*
     Displays the latest messages of all users.
    */
    public static Object public_timeline() {
        var rs = Queries.public_timeline();
        return null;
        //return render_template("templates/timeline.html");
    }


    /*
    Display's a users tweets.
     */
    static Object user_timeline(String username) {

        try {
            var profile_user =Queries.get_user(username);
            boolean followed = false;
            if (Queries.session != null && Queries.session.user != null) {
                followed = Queries.following(profile_user) != null;
            }
            var messages = Queries.messages(profile_user);

        } catch (SQLException throwables) {
            return "404 not found";
        }
        return render_template("timeline.html");
    }

    /*
    Adds the current user as follower of the given user.
     */
    static Object follow_user(String username) {

        var rs = Queries.follow_user(username);
        if (rs != null) {//flash('You are now following "%s"' % username)
        }
        else {return "404";}
        return null;
    }

    /*
    Removes the current user as follower of the given user.
     */
    static Object unfollow_user(String username) {
        var rs = Queries.follow_user(username);
        if (rs != null) {//flash('You are no longer following "%s"' % username)
        }
        else {return "404";}
        //return redirect(url_for('user_timeline', username=username))
        return null;
    }

    /*
    Registers a new message for the user.
     */
    static Object add_message(String message) {

        var rs = Queries.add_message(message);
        if (rs != null){
            //flash('You are no longer following "%s"' % username)
            //return redirect(url_for('user_timeline', username=username))
            //flash('Your message was recorded')
        }
        return timeline(null, null);
    }

    /*
    Logs the user in.
     */
    static Object login(String HTTPVerb, String username, String password1, String password2) {
        if (Queries.user_logged_in()) {
            return timeline(null, null);
        }

        String error = Queries.login(HTTPVerb, username, password1, password2);
        if(error.length()>0) System.out.println(error);

        String finalError = error; //must be effectively final
        return render_template("login.html", new HashMap<>() {{
            put("error", finalError);
        }});
    }

    /*
    Registers the user.
     */
    static Object register(String HTTPVerb, String username, String email, String password1, String password2) {
        if (Queries.user_logged_in()) {
            return timeline(null, null);
        }

        String error = Queries.register(HTTPVerb, username, email, password1, password2);
        if(error.length() > 0) System.out.println(error);

        String finalError = error; //must be effectively final
        return render_template("register.html", new HashMap<>() {{
            put("error", finalError);
        }});
    }

    /*
    Logs the user out
     */
    static Object logout() {
        //flash('You were logged out')
        Queries.session.user = null;
        return public_timeline();
    }

}
