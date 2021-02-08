package RoP;

public class Failure<T> extends Result<T> {

    Exception exception;

    public Failure(Exception e) {
        exception = e;
    }

    public Failure(String error) {
        exception = new Exception(error);
    }

    @Override
    public T get() {
        throw new IllegalStateException("evaluation was a failure");
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    public Exception getException() {
        return exception;
    }
}
