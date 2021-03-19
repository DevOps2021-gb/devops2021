package View;

import RoP.Failure;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Presentation {
    public static Object renderTemplate(String template, HashMap<String, Object> context) {
        try {
            Jinjava jinjava = new Jinjava();
            return jinjava.render(Resources.toString(Resources.getResource(template), StandardCharsets.UTF_8), context);
        } catch (IOException e) {
            e.printStackTrace();
            return new Failure<>(e);
        }
    }

    public static Object renderTemplate(String template) {
        return renderTemplate(template, new HashMap<>());
    }
}
