package benchmarking;
// Simple microbenchmark setups
// sestoft@itu.dk * 2013-06-02, 2015-09-15

import persistence.DB;
import java.util.function.IntToDoubleFunction;

class Benchmark {

  private static int n          = 10;
  private static double minTime = 1;
  private static final int USERS_TO_ADD     = 20_000;
  private static final int FOLLOWERS_TO_ADD = 40_000;
  private static final int MESSAGES_TO_ADD  = 40_000;

  public static void main(String[] args) {
    //setup
    final String[] usernames = CreateAndFillTestDB.genUsernames(USERS_TO_ADD);
    CreateAndFillTestDB.instantiateDB();
    //populateDB(usernames);
    var db = DB.connectDb().get();
    DB.addIndexes(db);
    System.out.println("start measureing");
    runBenchmarks(usernames);
  }
  private static void populateDB(String[] usernames) {
    DB.dropDB();
    System.out.println("start adding users");
    CreateAndFillTestDB.addUsers(USERS_TO_ADD, usernames);
    System.out.println("end adding users");
    System.out.println("start adding followers");
    CreateAndFillTestDB.addFollowers(FOLLOWERS_TO_ADD, usernames);
    System.out.println("end adding followers");
    System.out.println("start adding messages");
    CreateAndFillTestDB.addMessages(MESSAGES_TO_ADD, USERS_TO_ADD);
    System.out.println("end adding messages");
  }
  public static void runBenchmarks(String[] usernames){
    DBBenchmarkableFunctions.runCountUsers();
    systemInfo();
    printMark8Headers();
    mark8("GetUserId", i -> DBBenchmarkableFunctions.runGetUserId( USERS_TO_ADD, usernames));
    mark8("GetUser", i -> DBBenchmarkableFunctions.runGetUser( USERS_TO_ADD, usernames));
    mark8("GetUserById", i -> DBBenchmarkableFunctions.runGetUserById( USERS_TO_ADD));
    mark8("CountUsers", i -> DBBenchmarkableFunctions.runCountUsers());
    mark8("CountFollowers", i -> DBBenchmarkableFunctions.runCountFollowers());
    mark8("CountMessages", i -> DBBenchmarkableFunctions.runCountMessages());
    mark8("publicTimeline", i -> DBBenchmarkableFunctions.runPublicTimeline());
    mark8("TweetsByUsername", i -> DBBenchmarkableFunctions.runTweetsByUsername( USERS_TO_ADD, usernames));
    mark8("PersonalTweetsById", i -> DBBenchmarkableFunctions.runPersonalTweetsById( USERS_TO_ADD));
  }
  // ========== Infrastructure code ==========

  public static void systemInfo() {
    System.out.println("# OS:   "+
            System.getProperty("os.name")+"; "+
            System.getProperty("os.version")+"; "+
            System.getProperty("os.arch"));
    System.out.println("# JVM:  %s; %s%n"+
            System.getProperty("java.vendor")+"; "+
            System.getProperty("java.version"));
    // The processor identifier works only on MS Windows:
    System.out.println("# CPU:  "+
            System.getenv("PROCESSOR_IDENTIFIER")+"; cores:"+
            Runtime.getRuntime().availableProcessors());
  }
  public static void printMark8Headers(){
    System.out.println("msg, info,  mean, sdev, count");
  }

  public static double mark8(String msg, IntToDoubleFunction f) {
    int count = 1;
    int totalCount = 0;
    double dummy = 0.0;
    double runningTime;
    double st;
    double sst;
    double timeSpentPausingOnce = getTimeSpentPausingOnce();
    do {
      count *= 2;
      double timeSpentPausingCount = timeSpentPausingOnce*count;
      st = 0.0;
      sst = 0.0;
      Timer totalTime = new Timer();
      totalTime.play();
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++) {
          t.play();
          dummy += f.applyAsDouble(i);
          t.pause();
        }
        double time = Math.max((t.check()-timeSpentPausingCount) * 1e9 / count, 0.0);
        totalCount += count;
        st += time;
        sst += time * time;
      }
      totalTime.pause();
      runningTime = totalTime.check() / n;
    } while (runningTime < minTime && count < Integer.MAX_VALUE/2);
    computeResult(st, sst, msg, count);
    return dummy*totalCount;
  }
  public static double getTimeSpentPausingOnce(){
    Timer tForTimePausePlay = new Timer();
    long timesPaused = 10_000_000;
    for (int paused = 0; paused < timesPaused; paused++) { tForTimePausePlay.play(); tForTimePausePlay.pause(); }
    return 0.9*tForTimePausePlay.check() / timesPaused;    //0.9 to counter garbadge collection as that part rarely takes time normally
  }
  private static void computeResult(double st, double sst, String msg, int count) {
    double mean = st/n;
    double sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.println(msg+" "+mean+"ns "+sdev+" "+count);
  }

}