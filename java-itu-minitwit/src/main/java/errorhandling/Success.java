package errorhandling;

public class Success<T> implements Result<T> {
    private final T value;

    public Success(T val) {
        this.value = val;
    }

    @Override
    public T get() {
        return this.value;
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
