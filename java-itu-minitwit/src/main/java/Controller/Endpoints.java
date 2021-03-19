package Controller;

import Logic.Logger;
import Logic.Minitwit;
import Persistence.Repositories;
import Utilities.JSON;
import Utilities.Requests;
import View.Presentation;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Endpoints {
    private static final String[] entryPointsGetOrder     = new String[]{"/latest", "/msgs", "/msgs/:username", "/fllws/:username", "/", "/metrics", "/public", "/login", "/register", "/logout", "/:username/follow", "/:username/unfollow","/:username"};
    private static final String[] entryPointsPostOrder    = new String[]{"/msgs/:username","/fllws/:username","/add_message","/login","/register"};

    private static final Map<String, BiFunction<Request, Response, Object>> endpointsGet = new HashMap<>();
    private static final Map<String, BiFunction<Request, Response, Object>> endpointsPost =new HashMap<>();

    private static void setUpEntryPointsMap(){
        endpointsGet.put("/latest",              Endpoints::getLatest);
        endpointsGet.put("/msgs",                Endpoints::messages);
        endpointsGet.put("/msgs/:username",      Endpoints::messagesPerUser);
        endpointsGet.put("/fllws/:username",     Endpoints::getFollow);
        endpointsGet.put("/",                    Endpoints::timeline);
        endpointsGet.put("/metrics",             Endpoints::metrics);
        endpointsGet.put("/public",              Endpoints::publicTimeline);
        endpointsGet.put("/login",               Endpoints::loginGet);
        endpointsGet.put("/register",            (req, res)-> Presentation.renderTemplate(Minitwit.REGISTER_HTML));
        endpointsGet.put("/logout",              Endpoints::logout);
        endpointsGet.put("/:username/follow",    Endpoints::followUser);
        endpointsGet.put("/:username/unfollow",  Endpoints::unfollowUser);
        endpointsGet.put("/:username",           Endpoints::userTimeline);

        endpointsPost.put("/msgs/:username",     Endpoints::addMessage);
        endpointsPost.put("/fllws/:username",    Endpoints::postFollow);
        endpointsPost.put("/add_message",        Endpoints::addMessage);
        endpointsPost.put("/login",              Endpoints::login);
        endpointsPost.put("/register",           Endpoints::register);
    }

    public static Object getLatest(Request request, Response response) {
        return Minitwit.getLatest(response);
    }

    public static Object messages(Request request, Response response) {
        return Minitwit.messages(request, response);
    }

    public static Object messagesPerUser(Request request, Response response) {
        return Minitwit.messagesPerUser(request, response);
    }

    public static Object getFollow(Request request, Response response) {
        return Minitwit.getFollow(request, response);
    }

    public static Object timeline(Request request, Response response) {
        return Minitwit.timeline(request, response);
    }

    public static Object metrics(Request request, Response response) {
        return Minitwit.metrics(response);
    }

    public static Object publicTimeline(Request request, Response response) {
        return Minitwit.publicTimeline(request);
    }

    public static Object loginGet(Request request, Response response) {
        return Minitwit.loginGet(request);
    }

    public static Object logout(Request request, Response response) {
        return Minitwit.logout(request, response);
    }

    public static Object followUser(Request request, Response response) {
        return Minitwit.followUser(request, response);
    }

    public static Object unfollowUser(Request request, Response response) {
        return Minitwit.unfollowUser(request, response);
    }

    public static Object userTimeline(Request request, Response response) {
        return Minitwit.userTimeline(request);
    }

    public static Object addMessage(Request request, Response response) {
        return Minitwit.addMessage(request, response);
    }

    public static Object postFollow(Request request, Response response) {
        return Minitwit.postFollow(request, response);
    }

    public static Object login(Request request, Response response) {
        return Minitwit.login(request, response);
    }

    public static Object register(Request request, Response response) {
        return Minitwit.register(request, response);
    }

    public static void registerEndpoints() {
        setUpEntryPointsMap();
        for(String point : entryPointsGetOrder) {
            Spark.get(point, (req, res)-> Logger.benchMarkEndpoint(point, endpointsGet.get(point), req, res));
        }
        for(String point : entryPointsPostOrder) {
            Spark.post(point, (req, res)-> Logger.benchMarkEndpoint(point, endpointsPost.get(point), req, res));
        }
        Logger.setEndpointsToLog(entryPointsGetOrder, entryPointsPostOrder);
    }

    public static void registerHooks() {
        Spark.before((request, response) -> {
            Logger.processRequest();
            Logger.logRequest(request);

            Integer userId = Requests.getSessionUserId(request);
            if (userId != null) {
                var user = Repositories.getUserById(userId);
                if (user.isSuccess()) {
                    request.session().attribute(Minitwit.USER_ID, user.get().id);
                }
            }
        });

        Spark.notFound((req, res) -> {
            res.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE404_NOT_FOUND;
        });

        Spark.internalServerError((req, res) -> {
            res.type(JSON.APPLICATION_JSON);
            return JSON.MESSAGE500_SERVER_ERROR;
        });
    }
}
