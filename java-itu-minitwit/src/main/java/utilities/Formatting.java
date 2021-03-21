package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatting {

    private Formatting() {}

    /*
    Format a timestamp for display.
    */
    public static Result<String> formatDatetime(String timestamp) {
        try {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '@' HH:mm");
            Date resultDate = new Date(Long.parseLong(timestamp));
            //String date = sdf.format(resultDate);
            return new Success<>(resultDate.toString().substring(4, 21));
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }
}
