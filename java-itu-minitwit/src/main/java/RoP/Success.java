package RoP;

public class Success<T> implements Result<T> {
    private final T value;

    public Success(T val) {
        value = val;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public String getFailureMessage() {
        throw new IllegalStateException("Type is success with "+value+" not an error");
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
