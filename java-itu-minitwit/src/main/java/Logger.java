import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class Logger {
    private static final long LOGGING_PERIOD_SECONDS = 15;
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    static final Gauge cpuLoad = Gauge.build()
            .name("CPU_load").help("CPU load on server.").register();
    static final Counter requests = Counter.build()
            .name("requests_total").help("Total requests.").register();
    static final Gauge users = Gauge.build()
            .name("users_total").help("Total amount of users.").register();
    static final Gauge followers = Gauge.build()
            .name("followers_total").help("Total amount of followers.").register();
    static final Gauge messages = Gauge.build()
            .name("messages_total").help("Total amount of messages.").register();
    static final Gauge avgFollowers = Gauge.build()
            .name("followers_average").help("Average amount of followers per user.").register();
    static final Gauge responseTimePublicTimeLine = Gauge.build()
            .name("response_time_publicTIme").help("response time for public timeLine.").register();


    public static void StartLogging() throws IOException {
        System.out.println("Started logging information");
        StartSchedules();
    }

    public static void StartSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Logger::LogUserInformation, 1, LOGGING_PERIOD_SECONDS , TimeUnit.SECONDS);
    }
    private static void LogUserInformation() {
        processCpuLoad();
        processUsers();
        processFollowers();
        processMessages();
        processAvgFollowers();
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
    public static void processAvgFollowers(){
        long numberOfUsers = Queries.getCountUsers().get();
        long numberOfFollowers   = Queries.getCountFollowers().get();

        //if either are 0, the thread will throw an exception and exit
        if (numberOfFollowers != 0 && numberOfUsers != 0) {
            long num = numberOfFollowers/numberOfUsers;
            avgFollowers.set(num);
        }
    }

    public static void LogResponseTimeFrontPage(long rt) {
        responseTimePublicTimeLine.set(rt);
    }
}
