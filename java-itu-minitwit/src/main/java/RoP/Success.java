package RoP;

public class Success<T> extends Result<T> {
    private T value;

    public Success(T val) {
        value = val;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
