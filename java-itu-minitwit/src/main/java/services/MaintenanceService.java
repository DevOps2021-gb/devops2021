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
import repository.*;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import spark.Request;
import spark.Response;

public class MaintenanceService implements IMaintenanceService{

    private static final long LOGGING_PERIOD_SECONDS = 15;

    private static final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static final Gauge cpuLoad      = Gauge.build().name("CPU_load").help("CPU load on server.").register();
    private static final Counter requests   = Counter.build().name("requests_total").help("Total requests.").register();
    private static final Gauge users        = Gauge.build().name("users_total").help("Total amount of users.").register();
    private static final Gauge followers    = Gauge.build().name("followers_total").help("Total amount of followers.").register();
    private static final Gauge messages     = Gauge.build().name("messages_total").help("Total amount of messages.").register();

    private static final Map<String, Gauge> responseTimeEndPoints = new HashMap<>();

    private final IUserRepository userRepository;
    private final IMessageRepository messageRepository;
    private final IFollowerRepository followerRepository;
    private final ILogService logService;

    public MaintenanceService(IUserRepository _userRepository, IMessageRepository _messageRepository, IFollowerRepository _followerRepository, ILogService _logService) {
        userRepository = _userRepository;
        messageRepository = _messageRepository;
        followerRepository = _followerRepository;
        logService = _logService;
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

    public void startSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::logUserInformation, 1, LOGGING_PERIOD_SECONDS, TimeUnit.SECONDS);
    }
    private void logUserInformation() {
        processCpuLoad();
        processUsers();
        processFollowers();
        processMessages();
    }

    public void processRequest() {
        requests.inc();
    }
    public void processCpuLoad() {
        var cpuLoadLastMinute   = operatingSystemMXBean.getSystemLoadAverage() / operatingSystemMXBean.getAvailableProcessors();
        cpuLoad.set(cpuLoadLastMinute);
    }
    public void processUsers() {
        long numberOfUsers = userRepository.countUsers().get();
        users.set(numberOfUsers);
    }
    public void processFollowers() {
        long numberOfFollowers = followerRepository.countFollowers().get();
        followers.set(numberOfFollowers);
    }
    public void processMessages() {
        long numberOfMessages = messageRepository.countMessages().get();
        messages.set(numberOfMessages);
    }

    public static void logResponseTimeEndpoint(String endpoint, long rt) {
        responseTimeEndPoints.get(endpoint).set(rt);
    }

    public double getUsers() {
        return users.get();
    }
    public double getMessages() {
        return messages.get();
    }
    public double getFollowers() {
        return followers.get();
    }

    public Object benchMarkEndpoint(ResReqSparkWrapper rrw, String endPointName, BiFunction<Request, Response, Object> endpoint) {
        var startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = endpoint.apply(rrw.req, rrw.res);
        } catch (Exception e) {
            logService.logErrorWithMessage(e, "Endpoint error " + endPointName, Endpoints.class);
        }
        var endTime   = System.currentTimeMillis();
        try {
            MaintenanceService.logResponseTimeEndpoint(endPointToString(endPointName, rrw.isGet), endTime - startTime);
        } catch (Exception e) {
            logService.logErrorWithMessage(e, "Endpoint logging error " + endPointName, MaintenanceService.class);
        }
        return result;
    }

}
