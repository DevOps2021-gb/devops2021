package view;

import errorhandling.Failure;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import services.LogService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Presentation {

    private Presentation() {}

    public static Object renderTemplate(String template, Map<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), StandardCharsets.UTF_8), context);
        } catch (IOException e) {
            LogService.logError(e);
            return new Failure<>(e);
        }
    }

    public static Object renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }
}
