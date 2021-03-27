package main;

import controllers.Endpoints;
import services.LogService;
import persistence.DB;
import static spark.Spark.staticFiles;

public class Main {
    public static void main(String[] args) {
        try {
            staticFiles.location("/");
            handleArgs(args);
            Endpoints.registerHooks();
            Endpoints.registerEndpoints();

            //Add indexes to make sure they exits
            DB.addIndexes(DB.initDb());

            //add db clear here if working LOCALLY
            persistence.DB.dropDB();

            LogService.startSchedules();
        } catch (Exception e) {
            LogService.logError(e);
        }
    }
    public static void handleArgs(String[] args){
        if(args.length > 0) {
            DB.setCONNECTIONSTRING(args[0]);
            DB.setUSER(args[1]);
            DB.setPW(args[2]);
        }
    }
}
