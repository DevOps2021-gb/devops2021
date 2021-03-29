package utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONTests {
    @Test
    void isJSONGivenJsonReturnsTrue() {
        var actual = JSON.isJSON("{ hello: 1 }");

        Assertions.assertTrue(actual);
    }

    @Test
    void isJSONGivenRegularStringReturnsFalse() {
        var actual = JSON.isJSON("hello");

        Assertions.assertFalse(actual);
    }
}
