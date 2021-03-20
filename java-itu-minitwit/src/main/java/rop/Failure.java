package rop;

public class Failure<T> implements Result<T> {

    final Exception exception;

    public Failure(Exception e) {
        exception = e;
    }

    public Failure(String errorMsg) {
        exception = new Exception(errorMsg);
    }

    @Override
    public T get() {
        throw new IllegalStateException(exception.getMessage());
    }

    @Override
    public String getFailureMessage() {
        return exception.getMessage();
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
