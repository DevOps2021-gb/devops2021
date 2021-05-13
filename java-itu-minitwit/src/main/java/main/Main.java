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
import utilities.*;
import view.PresentationController;

import static spark.Spark.staticFiles;
import static spark.Spark.threadPool;

public class Main {

    public static final MutablePicoContainer container = new DefaultPicoContainer();

    public static void main(String[] args) {
        try {
            setupDI();

            staticFiles.location("/");
            setMaxThreads();

            container.getComponent(Endpoints.class).register();

            if (args.length > 0) {
                if(args.length == 4) {
                    var isNewDB = Boolean.parseBoolean(args[3]);
                    if(isNewDB) {
                        DB.dropDatabase();
                    }
                }
                DB.setDatabaseParameters(args[0], args[1], args[2]);
            }

            //Add indexes to make sure they exits
            DB.addIndexes(DB.initDatabase());

            container.getComponent(MaintenanceService.class).startSchedules();
        } catch (Exception e) {
            var logger = container.getComponent(LogService.class);
            logger.logError(e, Main.class);
        }
    }

    private static void setMaxThreads () {
        try {
            var maxThreads = 8;
            threadPool(maxThreads);
        } catch (IllegalStateException e) {
            var logger = container.getComponent(LogService.class);
            logger.logError(e, Main.class);
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
        container.addComponent(MetricsService.class);

        //Repositories
        container.addComponent(FollowerRepository.class);
        container.addComponent(MessageRepository.class);
        container.addComponent(UserRepository.class);

        //Utilities
        container.addComponent(Responses.class);
        container.addComponent(Requests.class);
        container.addComponent(JSONFormatter.class);
        container.addComponent(LogService.class);
        container.addComponent(Hashing.class);
        container.addComponent(Formatting.class);

        //View
        container.addComponent(PresentationController.class);

        //Other
        container.addComponent(DBBenchmarkableFunctions.class);
        container.addComponent(CreateAndFillTestDB.class);
    }
}
