
import RoP.Failure;
import RoP.Result;
import RoP.Success;
import org.junit.jupiter.api.Test;

class RoPTest {

    @Test
    void test_Success() {
        Result<Integer> v = new Success<>(2);
        assert v.isSuccess();
        assert v.get() == 2;
    }

    @Test
    void test_Failure() {
        var v1 = new Failure<Integer>(new IndexOutOfBoundsException("test"));
        assert !v1.isSuccess();
        assert v1.getException().getClass() == IndexOutOfBoundsException.class;
        assert v1.getFailureMessage().equals("test");


        var v2 = new Failure<Integer>("test");
        assert !v2.isSuccess();
        assert v2.getException().getClass() == Exception.class;
        assert v2.getFailureMessage().equals("test");
    }

}
