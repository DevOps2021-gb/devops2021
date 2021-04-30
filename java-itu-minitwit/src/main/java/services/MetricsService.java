package services;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import main.Main;

import java.io.IOException;
import java.io.StringWriter;

public class MetricsService implements IMetricsService{

    private final ILogService logService;

    public MetricsService(ILogService logService) {
        this.logService = logService;
    }

    public Object metrics() {
        final StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        } catch (IOException e) {
            logService.logError(e, Main.class);
        }
        return writer.toString();
    }

    public void updateLatest(String requestLatest) {
        if (requestLatest != null) {
            try {
                UserService.latest = Integer.parseInt(requestLatest);
            } catch (NumberFormatException e) {
                logService.logError(e, Main.class);
            }
        }
    }
}
