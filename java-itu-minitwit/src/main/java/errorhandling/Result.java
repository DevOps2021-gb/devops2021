package errorhandling;

public interface Result<T>
{
    public abstract T get();
    public abstract String getFailureMessage();
    public abstract boolean isSuccess();
}
