package benchmarking;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.IntToDoubleFunction;

public class BenchmarkableWrappers {
}

abstract class Benchmarkable implements IntToDoubleFunction {
    public abstract void setup();
    public abstract double applyAsDouble(int i);
}

abstract class BenchmarkableNoSetup extends Benchmarkable {
    public void setup(){}
}

abstract class BenchmarkableWithThreads extends Benchmarkable {
    public abstract void setup();
    public abstract double doAction(int threadIndex );

    final int NUM_THREADS;
    public BenchmarkableWithThreads(int NUM_THREADS){
        this.NUM_THREADS = NUM_THREADS;
    }
    public double applyAsDouble(int i) {
        ArrayList<Thread> allThreads = new ArrayList<>();
        for (int t =0; t < NUM_THREADS; t++) {
            final int threadIndex = t;
            Thread thread = new Thread(()->{ doAction(threadIndex); });
            thread.start();
            allThreads.add(thread);
        }
        for(Thread thread: allThreads) {
            try { thread.join();
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
        return allThreads.hashCode();
    }
}

abstract class BenchmarkableWithThreadsPreSetup extends Benchmarkable {
    public abstract double doAction(int threadIndex );

    final int NUM_THREADS;
    CyclicBarrier cb;
    ExecutorService exec;
    public BenchmarkableWithThreadsPreSetup(int NUM_THREADS){
        this.NUM_THREADS = NUM_THREADS;
    }
    public void setup(){
        cb = new CyclicBarrier(NUM_THREADS+1);
        exec = ForkJoinPool.commonPool(); //Executors.newFixedThreadPool(8);
        for (int t =0; t < NUM_THREADS; t++) {
            final int threadIndex = t;
            exec.submit(()->{
                try {
                    cb.await();
                    doAction(threadIndex);
                    cb.await();
                } catch (InterruptedException | BrokenBarrierException e) {e.printStackTrace();}
            });
        }
    }
    public double applyAsDouble(int i) {
        try {cb.await();cb.await();
        } catch (InterruptedException | BrokenBarrierException e) {e.printStackTrace();}
        return exec.hashCode();
    }
}