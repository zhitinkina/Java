package study.benchmarktool;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class BenchmarkTool {

    private static String url;
    private static long num;
    private static long concurrency;
    private static long timeout = 30000;
    private BenchmarkToolTimeProvider timeProvider = new SystemTimeBenchmarkToolTimeProvider();
    private PrintStream out = System.out;
    private HttpClient httpClient;

    public static void main(String[] args) throws Exception {
        BenchmarkTool benchmarkTool = new BenchmarkTool();
        benchmarkTool.setOptions(args);
        BenchmarkToolSummary benchmarkToolSummary = benchmarkTool.execute();

        DecimalFormat decimalFormat = new DecimalFormat("0.000");

        System.out.println("Количество потоков: " + benchmarkToolSummary.getThreadCount());
        System.out.println("Общее время теста: " + decimalFormat.format(benchmarkToolSummary.getTestTotalTime()) + " секунд");
        System.out.println("Количество запросов: " + benchmarkToolSummary.getTotalRequestCount());
        System.out.println("Количество неуспешных запросов: " + benchmarkToolSummary.getUnsuccessfulRequestCount());
        System.out.println("Количество переданных байт: " + benchmarkToolSummary.getTotalByteCount());
        System.out.println("Количество запросов в секунду: " + decimalFormat.format(benchmarkToolSummary.getRequestPerSecond()));
        System.out.println("Среднее время ответа: " + decimalFormat.format(benchmarkToolSummary.getAverageResponseTime()) + " секунд");
        System.out.println("50 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile50()) + " миллисекунд");
        System.out.println("80 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile80()) + " миллисекунд");
        System.out.println("90 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile90()) + " миллисекунд");
        System.out.println("95 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile95()) + " миллисекунд");
        System.out.println("99 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile99()) + " миллисекунд");
        System.out.println("100 процентиль: " + decimalFormat.format(benchmarkToolSummary.getPercentile100()) + " миллисекунд");
    }

    public void setOptions(String[] args) throws Exception {
        var options = new Options();
        options.addOption("u", "url", true, "URL web-сервиса");
        options.addOption("n", "num", true, "Общее количество запросов");
        options.addOption("c", "concurrency", true, "Количество потоков");
        options.addOption(
                "t",
                "timeout",
                true,
                "Максимальное количество миллисекунд, которое ожидается до того как считать запрос неуспешным"
        );

        var commandLine = new DefaultParser().parse(options, args, true);

        if (!commandLine.hasOption("u")) {
            throw new Exception("Укажите опцию url");
        }

        if (!commandLine.hasOption("n")) {
            throw new Exception("Укажите опцию num");
        }

        if (!commandLine.hasOption("c")) {
            throw new Exception("Укажите опцию concurrency");
        }

        url = commandLine.getOptionValue("u");
        num = Integer.parseInt(commandLine.getOptionValue("n"));
        concurrency = Integer.parseInt(commandLine.getOptionValue("c"));
        if (commandLine.hasOption("t")) {
            timeout = Integer.parseInt(commandLine.getOptionValue("t"));
        }
    }

    public BenchmarkToolSummary execute() {
        var requiredConcurrency = Math.min(getConcurrency(), getNum());
        var threadList = new ArrayList<Thread>();
        var benchmarkToolRunnableList = new ArrayList<BenchmarkToolRunnable>();
        var notCompletedRequestCount = new AtomicLong(getNum());

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (exceptionalThread, exception) -> {
            getOut().println("Один из потоков сотворил какую-то неконтролируемую дичь. Работа потоков остановливается...");
            interruptThreads(threadList);
        };

        HttpClient httpClient =
                getHttpClient() != null ? getHttpClient() :
                        HttpClient
                                .newBuilder()
                                .connectTimeout(Duration.ofSeconds(getTimeout()))
                                .followRedirects(HttpClient.Redirect.NEVER)
                                .build();

        for (int i = 0; i < requiredConcurrency; i++) {
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
            var benchmarkToolRunnable = new BenchmarkToolRunnable(
                    getTimeProvider(),
                    httpClient,
                    httpRequest,
                    notCompletedRequestCount
            );
            benchmarkToolRunnableList.add(benchmarkToolRunnable);

            var thread = new Thread(benchmarkToolRunnable, String.valueOf(i));
            threadList.add(thread);

            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);

            thread.start();
        }

        long startTime = getTimeProvider().getNano();

        try {
            for (Thread thread : threadList) {
                thread.join();
            }
        } catch (InterruptedException e) {
            getOut().println("Работа программы была неожиданно прервана");
            interruptThreads(threadList);
            throw new BenchmarkToolException("Работа программы была неожиданно прервана", e);
        }

        var totalTime = ((double) (getTimeProvider().getNano() - startTime) / 1_000_000_000);
        long totalRequestCount = 0;
        long unsuccessfulRequestCount = 0;
        long totalByteCount = 0;
        var responseTimeList = new ArrayList<Double>();
        for (BenchmarkToolRunnable benchmarkToolRunnable : benchmarkToolRunnableList) {
            totalRequestCount += benchmarkToolRunnable.getHttpResponses().size();
            for (HttpResponse httpResponse : benchmarkToolRunnable.getHttpResponses()) {
                responseTimeList.add((double) httpResponse.getResponseTime() / 1_000_000);
                if (httpResponse.getResponseCode() >= 400 || httpResponse.getResponseCode() == 0) {
                    unsuccessfulRequestCount++;
                }
                totalByteCount += httpResponse.getResponseByteCount();
            }
        }

        var responseTimes = new double[responseTimeList.size()];
        var responseTimeSum = 0d;
        for (int i = 0; i < responseTimes.length; i++) {
            var responseTime = responseTimeList.get(i);
            responseTimes[i] = responseTime;
            responseTimeSum += responseTime;
        }
        double averageResponseTime = responseTimeSum / responseTimes.length;
        double requestPerSecond = totalRequestCount / totalTime;

        Percentile percentile = new Percentile();

        return new BenchmarkToolSummary(
                getConcurrency(),
                totalTime,
                totalRequestCount,
                unsuccessfulRequestCount,
                totalByteCount,
                requestPerSecond,
                averageResponseTime,
                percentile.evaluate(responseTimes, 50),
                percentile.evaluate(responseTimes, 80),
                percentile.evaluate(responseTimes, 90),
                percentile.evaluate(responseTimes, 95),
                percentile.evaluate(responseTimes, 99),
                percentile.evaluate(responseTimes, 100)
        );
    }

    private void interruptThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                getOut().println("Сигнал остановки передан потоку " + thread.getName());
                thread.interrupt();
            }
        }
    }

    public BenchmarkToolTimeProvider getTimeProvider() { return timeProvider; }
    public void setTimeProvider(BenchmarkToolTimeProvider timeProvider) { this.timeProvider = timeProvider; }
    public PrintStream getOut() { return out; }
    public void setOut(PrintStream out) { this.out = out; }
    public HttpClient getHttpClient() { return httpClient; }
    public void setHttpClient(HttpClient httpClient) { this.httpClient = httpClient; }
    public String getUrl() { return url; }
    public long getNum() { return num; }
    public long getConcurrency() { return concurrency; }
    public long getTimeout() { return timeout; }
}
