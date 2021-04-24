package main;

import benchmarking.CreateAndFillTestDB;
import benchmarking.DBBenchmarkableFunctions;
import controllers.Endpoints;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import repository.FollowerRepository;
import repository.MessageRepository;
import repository.UserRepository;
import services.*;
import repository.DB;

import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

public class Main {

    public static MutablePicoContainer container = new DefaultPicoContainer();

    public static void main(String[] args) {
        try {
            setupDI();

            staticFiles.location("/");
            setMaxThreads();

            container.getComponent(Endpoints.class).register();

            if (args.length > 0) {
                DB.setDatabaseParameters(args[0], args[1], args[2]);
            }

            //Add indexes to make sure they exits
            DB.addIndexes(DB.initDatabase());

            container.getComponent(MaintenanceService.class).startSchedules();
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

    private static void setupDI() {
        //Controllers
        container.addComponent(Endpoints.class);

        //Services
        container.addComponent(MaintenanceService.class);
        container.addComponent(MessageService.class);
        container.addComponent(TimelineService.class);
        container.addComponent(UserService.class);

        //Repositories
        container.addComponent(FollowerRepository.class);
        container.addComponent(MessageRepository.class);
        container.addComponent(UserRepository.class);

        //Other
        container.addComponent(DBBenchmarkableFunctions.class);
        container.addComponent(CreateAndFillTestDB.class);
    }
}
