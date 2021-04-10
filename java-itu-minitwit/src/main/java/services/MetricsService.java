package services;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import main.Main;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.StringWriter;

public class MetricsService {

    private MetricsService() {}

    public static Object metrics() {
        //response.type(TextFormat.CONTENT_TYPE_004);
        final StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        } catch (IOException e) {
            LogService.logError(e, Main.class);
        }
        return writer.toString();
    }

    public static void updateLatest(String requestLatest) {
        if (requestLatest != null) {
            try {
                UserService.latest = Integer.parseInt(requestLatest);
            } catch (NumberFormatException e) {
                LogService.logError(e, Main.class);
            }
        }
    }
}
