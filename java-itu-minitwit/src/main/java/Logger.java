import java.io.File;
import java.io.FileWriter;
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

    private static final long LOGGING_PERIOD_SECONDS = 10;
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();


    static final Gauge cpuLoad = Gauge.build()
            .name("CPU_load").help("CPU load on server.").register();
    static final Counter requests = Counter.build()
            .name("requests_total").help("Total requests.").register();
    static final Gauge users = Gauge.build()
            .name("users_total").help("Total ammount of users.").register();
    static final Gauge avgFollowers = Gauge.build()
            .name("followers_average").help("Average ammount of followers per user.").register();
    static final Gauge responseTimePublicTimeLine = Gauge.build()
            .name("response_time_publicTIme").help("response time for public timeLine.").register();

    public static void StartSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(()->LogUserInformation(), 0, LOGGING_PERIOD_SECONDS, TimeUnit.SECONDS);
    }
    private static void LogUserInformation() {
        processCpuLoad();
        processUsers();
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
        int numberOfUsers = Queries.getAllUsers().get().size(); //todo make more efficient
        users.set(numberOfUsers);
    }
    public static void processAvgFollowers(){
        int numberOfUsers = Queries.getAllUsers().get().size(); //todo make more efficient
        int numberOfFollowers   = Queries.getAllFollowers().get().size();
        avgFollowers.set(numberOfFollowers/numberOfUsers);
    }

    public static void LogResponseTimeFrontPage(long rt) {
        responseTimePublicTimeLine.set(rt);
    }
}
