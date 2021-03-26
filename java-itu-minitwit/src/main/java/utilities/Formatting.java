package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import java.util.Date;

public class Formatting {

    private Formatting() {}

    /*
    Format a timestamp for display.
    */
    public static Result<String> formatDatetime(String timestamp) {
        try {
            Date resultDate = new Date(Long.parseLong(timestamp));
            return new Success<>(resultDate.toString().substring(4, 21));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }
}
