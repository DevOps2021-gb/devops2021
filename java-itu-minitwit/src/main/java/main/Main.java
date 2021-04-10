package main;

import controllers.Endpoints;
import services.LogService;
import services.MaintenanceService;
import repository.DB;
import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

public class Main {
    public static void main(String[] args) {
        try {

            staticFiles.location("/");
            setMaxThreads();
            Endpoints.register();

            if (args.length > 0) {
                DB.setDatabaseParameters(args[0], args[1], args[2]);
            }


            //Add indexes to make sure they exits
            DB.addIndexes(DB.initDatabase());

            MaintenanceService.startSchedules();
        } catch (Exception e) {
            LogService.logError(e, Main.class);
        }
    }

    private static void setMaxThreads () {
        try {
            int maxThreads = 8;
            threadPool(maxThreads);
        } catch (IllegalStateException e) {
            LogService.logError(e, Main.class);
        }
    }
}
