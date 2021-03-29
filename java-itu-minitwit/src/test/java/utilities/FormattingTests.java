package utilities;

import errorhandling.Failure;
import errorhandling.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Date;


public class FormattingTests {
    @Test
    void formatDatetimeGivenValidTimestampReturnsSuccess() {
        var time = new Date().getTime();
        var actual = Formatting.formatDatetime(String.valueOf(time));

        Assertions.assertSame(actual.getClass(), Success.class);
    }

    @Test
    void formatDatetimeGivenInvalidTimestampReturnsFailure() {
        var actual = Formatting.formatDatetime("timestamp");

        Assertions.assertSame(actual.getClass(), Failure.class);
    }
}
