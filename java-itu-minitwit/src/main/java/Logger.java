import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class Logger {
    private static final long LOGGING_PERIOD_SECONDS = 15;
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

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
    private static final Gauge responseTimePublicTimeLine = Gauge.build()
            .name("response_time_publicTIme").help("response time for public timeLine.").register();


    public static void startSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Logger::logUserInformation, 1, LOGGING_PERIOD_SECONDS , TimeUnit.SECONDS);
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
    public static void processCpuLoad(){
        var cpuLoadLastMinute   = operatingSystemMXBean.getSystemLoadAverage() / operatingSystemMXBean.getAvailableProcessors();
        cpuLoad.set(cpuLoadLastMinute);
    }
    public static void processUsers(){
        long numberOfUsers = Queries.getCountUsers().get();
        users.set(numberOfUsers);
    }
    public static void processFollowers(){
        long numberOfFollowers = Queries.getCountFollowers().get();
        followers.set(numberOfFollowers);
    }
    public static void processMessages(){
        long numberOfMessages = Queries.getCountMessages().get();
        messages.set(numberOfMessages);
    }

    public static void logResponseTimeFrontPage(long rt) {
        responseTimePublicTimeLine.set(rt);
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
}
