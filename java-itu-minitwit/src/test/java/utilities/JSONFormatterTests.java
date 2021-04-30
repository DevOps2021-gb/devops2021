package utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class JSONFormatterTests {

    IResponses responses = mock(Responses.class);

    private JSONFormatter getFormatter() {
        return new JSONFormatter(responses);
    }

    @Test
    void isJSONGivenJsonReturnsTrue() {
        var actual = getFormatter().isJSON("{ hello: 1 }");

        Assertions.assertTrue(actual);
    }

    @Test
    void isJSONGivenRegularStringReturnsFalse() {
        var actual = getFormatter().isJSON("hello");

        Assertions.assertFalse(actual);
    }
}
