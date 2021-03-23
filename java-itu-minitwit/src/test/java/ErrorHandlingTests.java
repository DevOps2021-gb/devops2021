
import errorhandling.Failure;
import errorhandling.Result;
import errorhandling.Success;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorHandlingTests {

    @Test
    void testSuccess() {
        final Result<Integer> success = new Success<>(2);
        Assertions.assertEquals(true, success.isSuccess());
        Assertions.assertEquals(true, success.get() == 2);
    }

    @Test
    void testFailure() {
        final var failureFromException = new Failure<Integer>(new IndexOutOfBoundsException("test"));
        Assertions.assertEquals(false, failureFromException.isSuccess());
        Assertions.assertEquals(IndexOutOfBoundsException.class, failureFromException.getException().getClass());
        Assertions.assertEquals("test", failureFromException.getFailureMessage());
        final var failureFromMessage = new Failure<Integer>("test");
        Assertions.assertEquals(false, failureFromMessage.isSuccess());
        Assertions.assertEquals(Exception.class, failureFromMessage.getException().getClass());
        Assertions.assertEquals("test", failureFromMessage.getFailureMessage());
    }

}
