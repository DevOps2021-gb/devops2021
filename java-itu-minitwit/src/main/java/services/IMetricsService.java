package services;

public interface IMetricsService {
    Object metrics();
    void updateLatest(String requestLatest);
}
