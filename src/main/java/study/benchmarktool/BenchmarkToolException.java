package study.benchmarktool;

public class BenchmarkToolException extends RuntimeException {

    public BenchmarkToolException() {
    }
    public BenchmarkToolException(String message) {
        super(message);
    }
    public BenchmarkToolException(String message, Throwable cause) {
        super(message, cause);
    }
    public BenchmarkToolException(Throwable cause) {
        super(cause);
    }
    public BenchmarkToolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
