package services;

public interface IMaintenanceService {
    void startSchedules();
    void processRequest();
    void processCpuLoad();
    void processUsers();
    void processMessages();
}
