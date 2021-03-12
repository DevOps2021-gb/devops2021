import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Time;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;

public class Logger {
    private static final long LOGGING_PERIOD_SECONDS = 30;
    /*private static FileWriter writeLogNumberOfUsers;
    private static FileWriter writeLogAvgNumberOfFollowers;
    private static FileWriter writeLogCPULoad;
    private static FileWriter writeLogResponseTimeFrontPage;*/
    private static Date logStartTime;
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    static final Gauge cpuLoad = Gauge.build()
            .name("CPU_load").help("CPU load on server.").register();
    static final Counter requests = Counter.build()
            .name("requests_total").help("Total requests.").register();
    static final Gauge users = Gauge.build()
            .name("users_total").help("Total amount of users.").register();
    static final Gauge avgFollowers = Gauge.build()
            .name("followers_average").help("Average amount of followers per user.").register();
    static final Gauge responseTimePublicTimeLine = Gauge.build()
            .name("response_time_publicTIme").help("response time for public timeLine.").register();


    public static void StartLogging() throws IOException {
        System.out.println("Started logging information");
        //MakeLogWriters();
        StartSchedules();
    }
    /*private static void MakeLogWriters(){
        File file = new File("Logs/");
        if (!file.exists()){
            while (!file.mkdir()){}
        }
        logStartTime = new Date();
        var dateString = new StringBuilder().append(logStartTime.getYear() - 100).append("-").append(logStartTime.getMonth()).append("-").append(logStartTime.getDay()).toString();
        try {
            writeLogNumberOfUsers           =  new FileWriter("Logs/numberOfUsers-"+dateString+".txt", true);
            writeLogAvgNumberOfFollowers    =  new FileWriter("Logs/numberOfFollowers-"+dateString+".txt", true);
            writeLogCPULoad                 =  new FileWriter("Logs/CPULoadEachMinute-"+dateString+".txt", true);
            writeLogResponseTimeFrontPage   =  new FileWriter("Logs/responseTimeFrontPage-"+dateString+".txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public static void StartSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Logger::LogUserInformation, 1, LOGGING_PERIOD_SECONDS , TimeUnit.SECONDS);
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

        //if either are 0, the thread will throw an exception and exit
        if (numberOfFollowers != 0 && numberOfFollowers != 0) {
            int num = numberOfFollowers/numberOfUsers;
            avgFollowers.set(num);
        }
    }

    public static void LogResponseTimeFrontPage(long rt) {
        responseTimePublicTimeLine.set(rt);
    }
}
