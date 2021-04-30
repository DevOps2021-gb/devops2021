package benchmarking;
// Simple microbenchmark setups
// sestoft@itu.dk * 2013-06-02, 2015-09-15

import main.Main;
import repository.DB;
import services.ILogService;
import services.LogService;

import java.util.function.IntToDoubleFunction;

class Benchmark {

  private static final int n          = 10;
  private static final int USERS_TO_ADD     = 20_000;
  private static final int FOLLOWERS_TO_ADD = 40_000;
  private static final int MESSAGES_TO_ADD  = 40_000;

  private final IDBBenchmarkableFunctions functions;
  private final ICreateAndFillTestDB testDB;
  private final ILogService logService;

  public Benchmark(IDBBenchmarkableFunctions functions, ICreateAndFillTestDB testDB, ILogService logService) {
    this.functions = functions;
    this.testDB = testDB;
    this.logService = logService;
  }

  public static void main(String[] args) {
    var benchmark = new Benchmark(
            Main.container.getComponent(DBBenchmarkableFunctions.class),
            Main.container.getComponent(CreateAndFillTestDB.class),
            Main.container.getComponent(LogService.class)
            );
    benchmark.init();
  }

  private void init() {
    //setup
    final String[] usernames = testDB.genUsernames(USERS_TO_ADD);
    testDB.instantiateDB();
    //populateDB(usernames);
    var db = DB.connectDb().get();
    DB.addIndexes(db);
    logService.log(Benchmark.class, "start measureing");

    runBenchmarks(usernames);
  }

  private void populateDB(String[] usernames) {
    DB.dropDatabase();
    logService.log(Benchmark.class, "start adding users");
    testDB.addUsers(usernames);
    logService.log(Benchmark.class, "end adding users");
    logService.log(Benchmark.class, "start adding followers");
    testDB.addFollowers(FOLLOWERS_TO_ADD, usernames);
    logService.log(Benchmark.class, "end adding followers");
    logService.log(Benchmark.class, "start adding messages");
    testDB.addMessages(MESSAGES_TO_ADD, USERS_TO_ADD);
    logService.log(Benchmark.class, "end adding messages");
  }
  public void runBenchmarks(String[] usernames){
    functions.runCountUsers();
    systemInfo();
    printMark8Headers();
    mark8("GetUserId", i -> functions.runGetUserId( USERS_TO_ADD, usernames));
    mark8("GetUser", i -> functions.runGetUser( USERS_TO_ADD, usernames));
    mark8("GetUserById", i -> functions.runGetUserById( USERS_TO_ADD));
    mark8("CountUsers", i -> functions.runCountUsers());
    mark8("CountFollowers", i -> functions.runCountFollowers());
    mark8("CountMessages", i -> functions.runCountMessages());
    mark8("publicTimeline", i -> functions.runPublicTimeline());
    mark8("TweetsByUsername", i -> functions.runTweetsByUsername( USERS_TO_ADD, usernames));
    mark8("PersonalTweetsById", i -> functions.runPersonalTweetsById( USERS_TO_ADD));
  }
  // ========== Infrastructure code ==========

  public void systemInfo() {
    logService.log(Benchmark.class, "# OS:   " +
            System.getProperty("os.name") + "; " +
            System.getProperty("os.version") + "; " +
            System.getProperty("os.arch"));
    logService.log(Benchmark.class, "# JVM:  %s; %s%n" +
            System.getProperty("java.vendor") + "; " +
            System.getProperty("java.version"));
    // The processor identifier works only on MS Windows:
    logService.log(Benchmark.class, "# CPU:  " +
            System.getenv("PROCESSOR_IDENTIFIER") + "; cores:" +
            Runtime.getRuntime().availableProcessors());
  }
  public void printMark8Headers(){
    logService.log(Benchmark.class, "msg, mean, sdev, count");
  }

  public double mark8(String msg, IntToDoubleFunction f) {
    int count = 1;
    int totalCount = 0;
    double dummy = 0.0;
    double runningTime;
    double st;
    double sst;
    double timeSpentPausingOnce = getTimeSpentPausingOnce();
    double minTime = 1;
    do {
      count *= 2;
      double timeSpentPausingCount = timeSpentPausingOnce*count;
      st = 0.0;
      sst = 0.0;
      var totalTime = new Timer();
      totalTime.play();
      for (int j=0; j<n; j++) {
        var t = new Timer();
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

  public double getTimeSpentPausingOnce(){
    var tForTimePausePlay = new Timer();
    long timesPaused = 10_000_000;
    for (int paused = 0; paused < timesPaused; paused++) { tForTimePausePlay.play(); tForTimePausePlay.pause(); }
    return 0.9*tForTimePausePlay.check() / timesPaused;    //0.9 to counter garbadge collection as that part rarely takes time normally
  }

  private void computeResult(double st, double sst, String msg, int count) {
    double mean = st/n;
    double sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    logService.log(Benchmark.class, msg+" "+mean+"ns "+sdev+" "+count);
  }

}
