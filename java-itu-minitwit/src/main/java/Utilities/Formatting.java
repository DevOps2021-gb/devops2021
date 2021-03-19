package Utilities;

import RoP.Failure;
import RoP.Result;
import RoP.Success;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Formatting {
    /*
    Format a timestamp for display.
    */
    public static Result<String> formatDatetime(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '@' HH:mm");
            Date resultDate = new Date(Long.parseLong(timestamp));
            String date = sdf.format(resultDate);
            return new Success<>(date);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }
}
