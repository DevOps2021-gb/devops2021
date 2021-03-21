package errorhandling;

public class Failure<T> implements Result<T> {

    final Exception exception;

    public Failure(Exception e) {
        this.exception = e;
    }

    public Failure(String errorMsg) {
        this.exception = new Exception(errorMsg);
    }

    @Override
    public T get() {
        throw new IllegalStateException(exception.getMessage());
    }

    @Override
    public String getFailureMessage() {
        return this.exception.getMessage();
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    public Exception getException() {
        return this.exception;
    }

    @Override
    public String toString() {
        return this.exception.toString();
    }
}
