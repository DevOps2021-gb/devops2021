import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {
    private static final long LOGGING_PERIOD_SECONDS = 30;
    private static FileWriter writeLogNumberOfUsers;
    private static FileWriter writeLogAvgNumberOfFollowers;
    private static FileWriter writeLogCPULoad;
    private static FileWriter writeLogResponseTimeFrontPage;
    private static Date logStartTime;
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();


    public static void StartLogging() throws IOException {
        System.out.println("Started logging information");
        MakeLogWriters();
        StartSchedules();
    }
    private static void MakeLogWriters(){
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
    }
    public static void StartSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(()->LogUserInformation(), 0, LOGGING_PERIOD_SECONDS, TimeUnit.SECONDS);
    }
    private static void LogUserInformation() {
        var date = new java.util.Date();
        if(logStartTime.getDay() != date.getDay())
            MakeLogWriters();
        try {
            int numberOfUsers       = Queries.getAllUsers().get().size();
            int numberOfFollowers   = Queries.getAllFollowers().get().size();
            var cpuLoadLastMinute   = operatingSystemMXBean.getSystemLoadAverage() / operatingSystemMXBean.getAvailableProcessors();

            WriteToFileWriter(writeLogNumberOfUsers,        date, new StringBuilder().append(numberOfUsers));
            WriteToFileWriter(writeLogAvgNumberOfFollowers, date, new StringBuilder().append((numberOfUsers==0)? 0: (numberOfFollowers + 0.0) / numberOfUsers) );
            WriteToFileWriter(writeLogCPULoad,              date, new StringBuilder().append(cpuLoadLastMinute));
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void LogResponseTimeFrontPage(float time){
        try {
            WriteToFileWriter(writeLogResponseTimeFrontPage, new Date(), new StringBuilder().append(time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void WriteToFileWriter(FileWriter fw, Date date, StringBuilder st) throws IOException {
        fw.write(st.append(" - ").append(date).append("\n").toString());
        fw.flush();
    }

}
