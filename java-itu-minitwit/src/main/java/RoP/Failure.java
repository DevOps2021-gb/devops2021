package RoP;

public class Failure<T> extends Result<T> {

    final Exception exception;

    public Failure(Exception e) {
        exception = e;
    }

    public Failure(String error) {
        exception = new Exception(error);
    }

    @Override
    public T get() {
        throw new IllegalStateException(exception.getMessage());
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return exception.toString();
    }
}
