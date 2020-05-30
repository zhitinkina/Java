package study.benchmarktool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MockedBenchmarkToolTimeProvider implements BenchmarkToolTimeProvider {

    private final ConcurrentMap<Long, AtomicInteger> atomicIntegersByThreadIds = new ConcurrentHashMap<>();

    @Override
    public long getNano() {
        return getAtomicIntegersByThreadIds().computeIfAbsent(
                Thread.currentThread().getId(),
                key -> new AtomicInteger()
        ).addAndGet(1_000_000);
    }

    public ConcurrentMap<Long, AtomicInteger> getAtomicIntegersByThreadIds() {
        return atomicIntegersByThreadIds;
    }
}
