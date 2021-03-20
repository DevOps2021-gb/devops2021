
import rop.*;
import org.junit.jupiter.api.Test;

class RoPTest {

    @Test
    void testSuccess() {
        final Result<Integer> success = new Success<>(2);
        assert success.isSuccess();
        assert success.get() == 2;
    }

    @Test
    void testFailure() {
        final var failure1 = new Failure<Integer>(new IndexOutOfBoundsException("test"));
        assert !failure1.isSuccess();
        assert failure1.getException().getClass() == IndexOutOfBoundsException.class;
        assert failure1.getFailureMessage().equals("test");
        final var failure2 = new Failure<Integer>("test");
        assert !failure2.isSuccess();
        assert failure2.getException().getClass() == Exception.class;
        assert failure2.getFailureMessage().equals("test");
    }

}
