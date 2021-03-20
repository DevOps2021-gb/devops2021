
import org.junit.jupiter.api.Test;
import rop.Failure;
import rop.Result;
import rop.Success;

class RoPTest {

    @Test
    void testSuccess() {
        final Result<Integer> success = new Success<>(2);
        assert success.isSuccess();
        assert success.get() == 2;
    }

    @Test
    void testFailure() {
        final var failureFromException = new Failure<Integer>(new IndexOutOfBoundsException("test"));
        assert !failureFromException.isSuccess();
        assert failureFromException.getException().getClass() == IndexOutOfBoundsException.class;
        assert failureFromException.getFailureMessage().equals("test");
        final var failureFromMessage = new Failure<Integer>("test");
        assert !failureFromMessage.isSuccess();
        assert failureFromMessage.getException().getClass() == Exception.class;
        assert failureFromMessage.getFailureMessage().equals("test");
    }

}
