package errorhandling;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorHandlingTests {

    @Test
    void test_Success() {
        Result<Integer> v = new Success<>(2);
        Assertions.assertTrue(v.isSuccess());
        Assertions.assertEquals((Integer) 2, v.get());
        try {
            v.getFailureMessage();
        } catch (Exception e) {
            Assertions.assertEquals(IllegalStateException.class, e.getClass());
        }
    }
    @Test
    void test_Failure() {
        var v1 = new Failure<Integer>(new IndexOutOfBoundsException("test"));
        Assertions.assertFalse(v1.isSuccess());
        Assertions.assertEquals(IndexOutOfBoundsException.class, v1.getException().getClass());
        Assertions.assertEquals("test", v1.getFailureMessage());
        try {
            v1.get();
        } catch (Exception e) {
            Assertions.assertEquals(IllegalStateException.class, e.getClass());
        }
        Assertions.assertEquals(v1.getException().toString(), v1.toString());

        var v2 = new Failure<Integer>("test");
        Assertions.assertFalse(v2.isSuccess());
        Assertions.assertEquals(Exception.class, v2.getException().getClass());
        Assertions.assertEquals("test", v2.getFailureMessage());
        try {
            v2.get();
        } catch (Exception e) {
            Assertions.assertEquals(IllegalStateException.class, e.getClass());
        }
        Assertions.assertEquals(v2.getException().toString(), v2.toString());
    }

}
