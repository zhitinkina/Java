package study.benchmarktool;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BenchmarkToolRunnableTest {

    @Test
    void getHttpResponses() {
        var notCompletedRequestCount = new AtomicLong(4);

        BenchmarkToolRunnable benchmarkToolRunnable = new BenchmarkToolRunnable(
                new MockedBenchmarkToolTimeProvider(),
                new MockedHttpClient(),
                new MockedHttpRequest(),
                notCompletedRequestCount
        );

        benchmarkToolRunnable.run();

        var httpResponses = benchmarkToolRunnable.getHttpResponses();
        assertEquals(4, httpResponses.size());

        assertEquals(200, httpResponses.get(0).getResponseCode());
        assertEquals(1_000_000, httpResponses.get(0).getResponseTime());
        assertEquals(1, httpResponses.get(0).getResponseByteCount());

        assertEquals(200, httpResponses.get(1).getResponseCode());
        assertEquals(1_000_000, httpResponses.get(1).getResponseTime());
        assertEquals(1, httpResponses.get(1).getResponseByteCount());

        assertEquals(500, httpResponses.get(2).getResponseCode());
        assertEquals(1_000_000, httpResponses.get(2).getResponseTime());
        assertEquals(2, httpResponses.get(2).getResponseByteCount());

        assertEquals(0, httpResponses.get(3).getResponseCode());
        assertEquals(1_000_000, httpResponses.get(3).getResponseTime());
        assertEquals(0, httpResponses.get(3).getResponseByteCount());
    }

    private static class MockedHttpClient extends HttpClient {

        private int sendMethodCallCount = 0;

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            sendMethodCallCount++;
            switch (sendMethodCallCount) {
                case 1:
                case 2:
                    return (HttpResponse<T>) new MockedHttpResponse(
                            200,
                            new byte[1]
                    );
                case 3:
                    return (HttpResponse<T>) new MockedHttpResponse(
                            500,
                            new byte[2]
                    );
                case 4:
                    throw new IOException("Ошибка ввода-вывода");
                default:
                    throw new RuntimeException("Что-то пошло не так");
            }
        }

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
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return null;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }
    }

    private static class MockedHttpRequest extends HttpRequest {

        @Override
        public Optional<BodyPublisher> bodyPublisher() {
            return Optional.empty();
        }

        @Override
        public String method() {
            return null;
        }

        @Override
        public Optional<Duration> timeout() {
            return Optional.empty();
        }

        @Override
        public boolean expectContinue() {
            return false;
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public Optional<HttpClient.Version> version() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }
    }
}