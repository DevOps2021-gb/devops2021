package RoP;

public abstract class Result<T>
{
    public abstract T get();
    public abstract String getFailureMessage();

    public abstract boolean isSuccess();
}
