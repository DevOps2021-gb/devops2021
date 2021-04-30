package view;

import java.util.Map;

public interface IPresentationController {
    Object renderTemplate(String template, Map<String, Object> context);
    Object renderTemplate(String template);
    void redirect(String route);
}
