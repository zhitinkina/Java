package study.benchmarktool;

public class HttpResponse {

    private final long responseCode;
    private final long responseTime;
    private final long responseByteCount;

    public HttpResponse(long responseCode, long responseTime, long responseByteCount) {
        this.responseCode = responseCode;
        this.responseTime = responseTime;
        this.responseByteCount = responseByteCount;
    }

    public long getResponseCode() {
        return responseCode;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public long getResponseByteCount() {
        return responseByteCount;
    }
}
