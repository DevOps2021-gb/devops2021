package services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import spark.Request;
import spark.Response;

public class LogService {
    private static final long LOGGING_PERIOD_SECONDS = 15;
    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static final Gauge cpuLoad = Gauge.build()
        .name("CPU_load").help("CPU load on server.").register();
    private static final Counter requests = Counter.build()
        .name("requests_total").help("Total requests.").register();
    private static final Gauge users = Gauge.build()
        .name("users_total").help("Total amount of users.").register();
    private static final Gauge followers = Gauge.build()
        .name("followers_total").help("Total amount of followers.").register();
    private static final Gauge messages = Gauge.build()
        .name("messages_total").help("Total amount of messages.").register();
    private static final Map<String, Gauge> responseTimeEndPoints = new HashMap<>();
    private static final Logger logger = Logger.getLogger(LogService.class.getSimpleName());

    private LogService() {
    }

    public static void logError(Exception e) {
        logger.log(Level.INFO,e.getMessage());
    }

    public static void logRequest(Request request) {
        if (request.url().contains("favicon.ico")) return;

        if (request.params().size() == 0) {
            logger.log(Level.INFO, request.requestMethod() + " " + request.url());
        } else {
            logger.log(Level.INFO, request.requestMethod() + " with args " + request.params().toString().replaceAll("[\n\r\t]", "_"));
        }
    }

    private static void setEndpoints(String[] endpoints, String namePrefix, String helpPrefix) {
        for (String key: endpoints){
            String endPoint     = namePrefix + key.replace('/', '_');
            String helpString   = helpPrefix + key;
            Gauge gauge = Gauge.build()
                .name(endPoint).help(helpString).register();
            responseTimeEndPoints.put(key, gauge);
        }
    }

    public static void setEndpointsToLog(String[] endpointsGet, String[] endpointsPost) {
        setEndpoints(endpointsGet,  "response_time_get",    "response time for get call: ");
        setEndpoints(endpointsPost, "response_time_post",   "response time for post call: ");
    }

    public static void startSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(LogService::logUserInformation, 1, LOGGING_PERIOD_SECONDS , TimeUnit.SECONDS);
    }
    private static void logUserInformation() {
        processCpuLoad();
        processUsers();
        processFollowers();
        processMessages();
    }

    public static void processRequest() {
        requests.inc();
    }
    public static void processCpuLoad() {
        var cpuLoadLastMinute   = operatingSystemMXBean.getSystemLoadAverage() / operatingSystemMXBean.getAvailableProcessors();
        cpuLoad.set(cpuLoadLastMinute);
    }
    public static void processUsers() {
        long numberOfUsers = UserRepository.countUsers().get();
        users.set(numberOfUsers);
    }
    public static void processFollowers() {
        long numberOfFollowers = FollowerRepository.countFollowers().get();
        followers.set(numberOfFollowers);
    }
    public static void processMessages() {
        long numberOfMessages = MessageRepository.countMessages().get();
        messages.set(numberOfMessages);
    }

    public static void logResponseTimeEndpoint(String endpoint, long rt) {
        responseTimeEndPoints.get(endpoint).set(rt);
    }

    public static double getUsers() {
        return users.get();
    }
    public static double getMessages() {
        return messages.get();
    }
    public static double getFollowers() {
        return followers.get();
    }

    public static Object benchMarkEndpoint(String endPointName, BiFunction<Request, Response, Object> endpoint, Request req, Response res) {
        var startTime = System.currentTimeMillis();
        Object result = endpoint.apply(req, res);
        LogService.logResponseTimeEndpoint(endPointName, System.currentTimeMillis() - startTime);
        return result;
    }

}
