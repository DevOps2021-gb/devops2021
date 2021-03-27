package services;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.io.StringWriter;

public class MetricsService {

    private MetricsService() {}

    public static Object metrics(Response response) {
        response.type(TextFormat.CONTENT_TYPE_004);
        final StringWriter writer = new StringWriter();
        try {
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        } catch (IOException e) {
            LogService.logError(e);
        }
        return writer.toString();
    }

    public static void updateLatest(Request request) {
        String requestLatest = request.queryParams("latest");
        if (requestLatest != null) {
            try {
                UserService.latest = Integer.parseInt(requestLatest);
            } catch (NumberFormatException e) {
                LogService.logError(e);
            }
        }
    }
}
