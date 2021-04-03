package main;

import controllers.Endpoints;
import services.LogService;
import persistence.DB;
import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

public class Main {
    public static void main(String[] args) {
        try {

            staticFiles.location("/");
            Endpoints.registerEndpoints();
            Endpoints.registerHooks();

            if (args.length > 0) {
                DB.setDatabaseParameters(args[0], args[1], args[2]);
            }

            int maxThreads = 4;
            threadPool(maxThreads);

            //Add indexes to make sure they exits
            DB.addIndexes(DB.initDatabase());

            LogService.startSchedules();
        } catch (Exception e) {
            LogService.logError(e);
        }
    }
}
