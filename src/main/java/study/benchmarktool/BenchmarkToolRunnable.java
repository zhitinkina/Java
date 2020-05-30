package study.benchmarktool;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkToolRunnable implements Runnable {

    private final BenchmarkToolTimeProvider timeProvider;
    private final HttpClient httpClient;
    private final HttpRequest httpRequest;
    private final AtomicLong notCompletedRequestCount;
    private final List<HttpResponse> httpResponses = new ArrayList<>();

    public BenchmarkToolRunnable(
            BenchmarkToolTimeProvider timeProvider,
            HttpClient httpClient,
            HttpRequest httpRequest,
            AtomicLong notCompletedRequestCount
    ) {
        this.timeProvider = timeProvider;
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.notCompletedRequestCount = notCompletedRequestCount;
    }

    @Override
    public void run() {
        while (
                getNotCompletedRequestCount().decrementAndGet() >= 0 && !Thread.currentThread().isInterrupted()
        ) {
            long startTime = getTimeProvider().getNano();
            try {
                var httpResponse = getHttpClient().send(getHttpRequest(), BodyHandlers.ofByteArray());

                getHttpResponses().add(new HttpResponse(
                        httpResponse.statusCode(),
                        getTimeProvider().getNano() - startTime,
                        httpResponse.body().length
                ));
            } catch (IOException e) {
                getHttpResponses().add(new HttpResponse(
                        0,
                        getTimeProvider().getNano() - startTime,
                        0
                ));
            } catch (InterruptedException e) {
                System.out.println("Поток " + Thread.currentThread().getName() + " был остановлен");
                return;
            }
        }
    }

    private BenchmarkToolTimeProvider getTimeProvider() { return timeProvider; }
    private HttpClient getHttpClient() { return httpClient; }
    private HttpRequest getHttpRequest() { return httpRequest; }
    private AtomicLong getNotCompletedRequestCount() {
        return notCompletedRequestCount;
    }
    public List<HttpResponse> getHttpResponses() {
        return httpResponses;
    }
}
