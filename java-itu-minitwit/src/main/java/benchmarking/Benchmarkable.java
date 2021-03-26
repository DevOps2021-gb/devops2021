package benchmarking;

import java.util.function.IntToDoubleFunction;

abstract class Benchmarkable implements IntToDoubleFunction {
    public abstract void setup();
    public abstract double applyAsDouble(int i);
}
