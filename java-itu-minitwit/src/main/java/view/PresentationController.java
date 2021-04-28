package view;

import errorhandling.Failure;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import services.ILogService;
import services.LogService;
import utilities.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PresentationController implements IPresentationController {

    private final ILogService logService;

    public PresentationController(ILogService _logService) {
        logService = _logService;
    }

    public Object renderTemplate(String template, Map<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), StandardCharsets.UTF_8), context);
        } catch (IOException e) {
            logService.logError(e, PresentationController.class);
            return new Failure<>(e);
        }
    }

    public Object renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }

    public void redirect(String route) {
        Session.getSessionResponse().redirect(route);
    }
}
