package study.benchmarktool;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkToolTest {

    @Test
    void setOptions() throws Exception {
        BenchmarkTool benchmarkTool = new BenchmarkTool();
        benchmarkTool.setTimeProvider(new MockedBenchmarkToolTimeProvider());
        benchmarkTool.setOut(System.out);

        benchmarkTool.setOptions(
                new String[]{"--url", "http://example.com/", "--num", "10", "--concurrency", "5", "--timeout", "1000"}
        );

        assertEquals("http://example.com/", benchmarkTool.getUrl());
        assertEquals(10, benchmarkTool.getNum());
        assertEquals(5, benchmarkTool.getConcurrency());
        assertEquals(1000, benchmarkTool.getTimeout());
    }

    @Test
    void execute() throws Exception {
        var expectedTotalTime = (double) 1 / 1000;
        var expectedTotalRequestCount = 10;

        BenchmarkTool benchmarkTool = new BenchmarkTool();
        benchmarkTool.setTimeProvider(new MockedBenchmarkToolTimeProvider());
        benchmarkTool.setOut(System.out);
        benchmarkTool.setHttpClient(new MockedHttpClient());

        benchmarkTool.setOptions(
                new String[]{"--url", "http://example.com/", "--num", "10", "--concurrency", "5", "--timeout", "1000"}
        );

        BenchmarkToolSummary benchmarkToolSummary = benchmarkTool.execute();

        assertEquals(5, benchmarkToolSummary.getThreadCount());
        assertEquals(expectedTotalTime, benchmarkToolSummary.getTestTotalTime());
        assertEquals(expectedTotalRequestCount, benchmarkToolSummary.getTotalRequestCount());
        assertEquals(6, benchmarkToolSummary.getUnsuccessfulRequestCount());
        assertEquals(12, benchmarkToolSummary.getTotalByteCount());
        assertEquals(expectedTotalRequestCount / expectedTotalTime, benchmarkToolSummary.getRequestPerSecond());
        assertEquals(1.0, benchmarkToolSummary.getAverageResponseTime());
        assertEquals(1.0, benchmarkToolSummary.getPercentile50());
        assertEquals(1.0, benchmarkToolSummary.getPercentile80());
        assertEquals(1.0, benchmarkToolSummary.getPercentile90());
        assertEquals(1.0, benchmarkToolSummary.getPercentile95());
        assertEquals(1.0, benchmarkToolSummary.getPercentile100());

    }

    private static class MockedHttpClient extends HttpClient {

        private final AtomicInteger requestNumber = new AtomicInteger();

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return null;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            var requestNumber = getRequestNumber().incrementAndGet();

            switch (requestNumber) {
                case 1:
                case 2:
                case 3:
                case 4:
                    return (HttpResponse<T>) new MockedHttpResponse(
                            200,
                            new byte[1]
                    );
                case 5:
                case 6:
                case 7:
                case 8:
                    return (HttpResponse<T>) new MockedHttpResponse(
                            500,
                            new byte[2]
                    );
                case 9:
                case 10:
                    throw new IOException();
                default:
                    throw new RuntimeException("Что-то пошло не так");
            }
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            return null;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            return null;
        }

        private AtomicInteger getRequestNumber() {
            return requestNumber;
        }
    }
}