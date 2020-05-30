package study.benchmarktool;

public class BenchmarkToolSummary {

    private final long threadCount;
    private final double testTotalTime;
    private final long totalRequestCount;
    private final long unsuccessfulRequestCount;
    private final long totalByteCount;
    private final double requestPerSecond;
    private final double averageResponseTime;
    private final double percentile50;
    private final double percentile80;
    private final double percentile90;
    private final double percentile95;
    private final double percentile99;
    private final double percentile100;

    public BenchmarkToolSummary(
            long threadCount,
            double testTotalTime,
            long totalRequestCount,
            long unsuccessfulRequestCount,
            long totalByteCount,
            double requestPerSecond,
            double averageResponseTime,
            double percentile50,
            double percentile80,
            double percentile90,
            double percentile95,
            double percentile99,
            double percentile100
    ) {
        this.threadCount = threadCount;
        this.testTotalTime = testTotalTime;
        this.totalRequestCount = totalRequestCount;
        this.unsuccessfulRequestCount = unsuccessfulRequestCount;
        this.totalByteCount = totalByteCount;
        this.requestPerSecond = requestPerSecond;
        this.averageResponseTime = averageResponseTime;
        this.percentile50 = percentile50;
        this.percentile80 = percentile80;
        this.percentile90 = percentile90;
        this.percentile95 = percentile95;
        this.percentile99 = percentile99;
        this.percentile100 = percentile100;
    }

    public long getThreadCount() {
        return threadCount;
    }
    public double getTestTotalTime() {
        return testTotalTime;
    }
    public long getTotalRequestCount() {
        return totalRequestCount;
    }
    public long getUnsuccessfulRequestCount() {
        return unsuccessfulRequestCount;
    }
    public long getTotalByteCount() {
        return totalByteCount;
    }
    public double getRequestPerSecond() {
        return requestPerSecond;
    }
    public double getAverageResponseTime() {
        return averageResponseTime;
    }
    public double getPercentile50() {
        return percentile50;
    }
    public double getPercentile80() {
        return percentile80;
    }
    public double getPercentile90() {
        return percentile90;
    }
    public double getPercentile95() {
        return percentile95;
    }
    public double getPercentile99() {
        return percentile99;
    }
    public double getPercentile100() {
        return percentile100;
    }
}
