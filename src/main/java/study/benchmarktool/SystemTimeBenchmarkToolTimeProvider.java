package study.benchmarktool;

public class SystemTimeBenchmarkToolTimeProvider implements BenchmarkToolTimeProvider {

    @Override
    public long getNano() {
        return System.nanoTime();
    }
}
