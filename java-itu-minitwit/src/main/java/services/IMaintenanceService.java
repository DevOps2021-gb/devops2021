package services;

import controllers.ResReqSparkWrapper;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

public interface IMaintenanceService {
    void startSchedules();
    void processRequest();
    void processCpuLoad();
    void processUsers();
    void processMessages();
    double getUsers();
    double getMessages();
    double getFollowers();
    Object benchMarkEndpoint(ResReqSparkWrapper rrw, String endPointName, BiFunction<Request, Response, Object> endpoint);
}
