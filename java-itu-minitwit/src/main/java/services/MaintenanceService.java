package services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import controllers.Endpoints;
import controllers.ResReqSparkWrapper;
import persistence.FollowerRepository;
import persistence.MessageRepository;
import persistence.UserRepository;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import spark.Request;
import spark.Response;

public class MaintenanceService {
    private static final long LOGGING_PERIOD_SECONDS = 15;

    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static final Gauge cpuLoad      = Gauge.build().name("CPU_load").help("CPU load on server.").register();
    private static final Counter requests   = Counter.build().name("requests_total").help("Total requests.").register();
    private static final Gauge users        = Gauge.build().name("users_total").help("Total amount of users.").register();
    private static final Gauge followers    = Gauge.build().name("followers_total").help("Total amount of followers.").register();
    private static final Gauge messages     = Gauge.build().name("messages_total").help("Total amount of messages.").register();

    private static final Map<String, Gauge> responseTimeEndPoints = new HashMap<>();

    private MaintenanceService() {
    }

    private static void setEndpoints(String[] endpoints, Boolean isGet, String helpPrefix) {
        for (String endpointOriginal: endpoints){
            String endPoint     = endPointToString(endpointOriginal, isGet);
            String helpString   = helpPrefix + endpointOriginal;
            Gauge gauge = Gauge.build()
                .name(endPoint).help(helpString).register();
            responseTimeEndPoints.put(endPoint, gauge);
        }
    }
    private static String endPointToString(String endPoint, Boolean isGet) {
        String namePrefix = (isGet.equals(true))? "response_time_get" : "response_time_post";
        return new StringBuilder(namePrefix).append(endPoint.replace('/', '_')).toString();

    }

    public static void setEndpointsToLog(String[] endpointsGet, String[] endpointsPost) {
        setEndpoints(endpointsGet,  true,    "response time for get call: ");
        setEndpoints(endpointsPost, false,   "response time for post call: ");
    }

    public static void startSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(MaintenanceService::logUserInformation, 1, LOGGING_PERIOD_SECONDS , TimeUnit.SECONDS);
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

    public static Object benchMarkEndpoint(ResReqSparkWrapper rrw, String endPointName, BiFunction<Request, Response, Object> endpoint) {
        var startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = endpoint.apply(rrw.req, rrw.res);
        } catch (Exception e) {
            LogService.logErrorWithMessage(e, new StringBuilder("Endpoint error ").append(endPointName).toString(), Endpoints.class);
        }
        var endTime   = System.currentTimeMillis();
        try {
            MaintenanceService.logResponseTimeEndpoint(endPointToString(endPointName, rrw.isGet), endTime - startTime);
        } catch (Exception e) {
            LogService.logErrorWithMessage(e, new StringBuilder("Endpoint logging error ").append(endPointName).toString(), MaintenanceService.class);
        }
        return result;
    }

}
