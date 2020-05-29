package study.benchmarktool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkToolRunnable implements Runnable {

    private final String url;
    private final long timeout;
    private final AtomicLong notCompletedRequestCount;
    private final List<HttpResponse> httpResponses = new ArrayList<>();

    public BenchmarkToolRunnable(String url, long timeout, AtomicLong notCompletedRequestCount) {
        this.url = url;
        this.timeout = timeout;
        this.notCompletedRequestCount = notCompletedRequestCount;
    }

    @Override
    public void run() {
        HttpClient httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(getTimeout()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest httpRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(getUrl()))
                .header("Accept", "*/*")
                .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36"
                )
                .build();

        while (
                getNotCompletedRequestCount().decrementAndGet() >= 0 && /*Проверка, что можно работать дальше*/!Thread.currentThread().isInterrupted()
        ) {
            long startTime = System.nanoTime();
            try {
                var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());

                getHttpResponses().add(new HttpResponse(
                        httpResponse.statusCode(),
                        System.nanoTime() - startTime,
                        httpResponse.body().length
                ));
            } catch (IOException e) {
                getHttpResponses().add(new HttpResponse(
                        0,
                        System.nanoTime() - startTime,
                        0
                ));
            } catch (InterruptedException e) {
                System.out.println("Поток " + Thread.currentThread().getName() + " был остановлен");
                return;
            }
        }
    }

    private String getUrl() {
        return url;
    }
    private long getTimeout() {
        return timeout;
    }
    private AtomicLong getNotCompletedRequestCount() {
        return notCompletedRequestCount;
    }
    public List<HttpResponse> getHttpResponses() {
        return httpResponses;
    }
}
