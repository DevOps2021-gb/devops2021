package utilities;

import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatting {

    private Formatting() {}

    private static final String TIMESTAMP_PATTERN = "dd-MM-yyy HH:mm a";

    public static Result<String> formatDatetime(String timestamp) {
        try {
            Date resultDate = new Date(Long.parseLong(timestamp));
            SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.ENGLISH);
            String formattedDate = sdf.format(resultDate);

            return new Success<>(formattedDate);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }
}
