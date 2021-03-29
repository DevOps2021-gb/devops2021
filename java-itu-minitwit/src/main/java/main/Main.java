package main;

import controllers.Endpoints;
import services.LogService;
import persistence.DB;
import static spark.Spark.staticFiles;

public class Main {
    public static void main(String[] args) {
        try {
            staticFiles.location("/");

            if (args.length > 0) {
                DB.setDatabaseParameters(args[0], args[1], args[2]);
            }

            Endpoints.init();

            //add db clear here if working LOCALLY
            //DB.dropDatabase();

            //Add indexes to make sure they exits
            //DB.addIndexes(DB.initDatabase());
            LogService.startSchedules();
        } catch (Exception e) {
            LogService.logError(e);
        }
    }
}
