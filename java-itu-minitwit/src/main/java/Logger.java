import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {
    private static FileWriter writeLogNumberOfUsers;
    private static FileWriter writeLogAvgNumberOfFollowers;
    private static Date logStartTime;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void StartLogging() throws IOException {
        System.out.println("Started logging information");
        MakeLogWriters();
        StartSchedules();
    }
    private static void MakeLogWriters(){
        logStartTime = new Date();
        var dateString = new StringBuilder().append(logStartTime.getYear() - 100).append("-").append(logStartTime.getMonth()).append("-").append(logStartTime.getDay()).toString();
        try {
            writeLogNumberOfUsers        =  new FileWriter("Logs/numberOfUsers-"+dateString+".txt", true);
            writeLogAvgNumberOfFollowers =  new FileWriter("Logs/numberOfFollowers-"+dateString+".txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void StartSchedules() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(()->LogUserInformation(), 0, 1, TimeUnit.SECONDS);
    }
    private static void LogUserInformation() {
        StringBuilder st2 = new StringBuilder();
        var date = new java.util.Date();
        if(logStartTime.getDay() != date.getDay())
            MakeLogWriters();
        try {
            int numberOfUsers       = Queries.getAllUsers().get().size();
            int numberOfFollowers   = Queries.getAllFollowers().get().size();

            WriteToFileWriter(writeLogNumberOfUsers,        date, new StringBuilder().append(numberOfUsers));
            WriteToFileWriter(writeLogAvgNumberOfFollowers, date, new StringBuilder().append((numberOfFollowers==0)? 0: (numberOfUsers + 0.0) / numberOfFollowers) );
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void WriteToFileWriter(FileWriter fw, Date date, StringBuilder st) throws IOException {
        fw.write(st.append(" - ").append(date).append("\n").toString());
        fw.flush();
    }

}
