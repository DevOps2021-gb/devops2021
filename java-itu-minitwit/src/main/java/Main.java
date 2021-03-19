import Controller.Endpoints;
import Logic.Logger;
import Persistence.DB;

import static spark.Spark.staticFiles;

public class Main {
    public static void main(String[] args) {
        try {
            staticFiles.location("/");

            if(args.length > 0) {
                DB.setCONNECTIONSTRING(args[0]);
                DB.setUSER(args[1]);
                DB.setPW(args[2]);
            }

            Endpoints.registerHooks();
            Endpoints.registerEndpoints();

            //add db clear here if working LOCALLY

            Logger.startSchedules();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
