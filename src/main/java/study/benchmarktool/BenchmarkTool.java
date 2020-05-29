package study.benchmarktool;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BenchmarkTool {

    private static String url;
    private static long num;
    private static long concurrency;
    private static long timeout = 30000;

    public static void main(String[] args) throws Exception {
        setOptions(args);
        execute();
    }

    private static void setOptions(String[] args) throws Exception {
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

        setUrl(commandLine.getOptionValue("u"));
        setNum(Integer.parseInt(commandLine.getOptionValue("n")));
        setConcurrency(Integer.parseInt(commandLine.getOptionValue("c")));
        if (commandLine.hasOption("t")) {
            setTimeout(Integer.parseInt(commandLine.getOptionValue("t")));
        }
    }

    private static void execute() {
        var requiredConcurrency = Math.min(getConcurrency(), getNum());
        var threadList = new ArrayList<Thread>();
        var benchmarkToolRunnableList = new ArrayList<BenchmarkToolRunnable>();
        var notCompletedRequestCount = new AtomicLong(getNum());

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (exceptionalThread, exception) -> {
            System.out.println("Один из потоков сотворил какую-то неконтролируемую дичь. Работа потоков остановливается...");
            interruptThreads(threadList);
        };

        for (int i = 0; i < requiredConcurrency; i++) {
            var benchmarkToolRunnable = new BenchmarkToolRunnable(
                    getUrl(),
                    getTimeout(),
                    notCompletedRequestCount
            );
            benchmarkToolRunnableList.add(benchmarkToolRunnable);

            var thread = new Thread(benchmarkToolRunnable, String.valueOf(i));
            threadList.add(thread);
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            thread.start();
        }

        long startTime = System.nanoTime();

        try {
            for (Thread thread : threadList) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Работа программы была неожиданно прервана");
            interruptThreads(threadList);
            return;
        }

        var totalTime = ((double) (System.nanoTime() - startTime) / 1_000_000_000);
        long totalRequestCount = 0;
        long unsuccessfulRequestCount = 0;
        long totalByteCount = 0;

        var responseTimeList = new ArrayList<Double>();
        for (BenchmarkToolRunnable benchmarkToolRunnable : benchmarkToolRunnableList) {
            totalRequestCount += benchmarkToolRunnable.getHttpResponses().size();
            for (HttpResponse httpResponse : benchmarkToolRunnable.getHttpResponses()) {
                responseTimeList.add((double) httpResponse.getResponseTime() / 1_000_000);
                if (httpResponse.getResponseCode() >= 400) {
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

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        Percentile percentile = new Percentile();

        System.out.println("Общее время теста: " + decimalFormat.format(totalTime) + " секунд");
        System.out.println("Количество запросов: " + totalRequestCount);
        System.out.println("Количество неуспешных запросов: " + unsuccessfulRequestCount);
        System.out.println("Количество переданных байт: " + totalByteCount);
        System.out.println("Количество запросов в секунду: " + decimalFormat.format(requestPerSecond));
        System.out.println("Среднее время ответа: " + decimalFormat.format(averageResponseTime) + " секунд");
        System.out.println("50 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 50)) + " миллисекунд");
        System.out.println("80 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 80)) + " миллисекунд");
        System.out.println("90 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 90)) + " миллисекунд");
        System.out.println("95 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 95)) + " миллисекунд");
        System.out.println("99 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 99)) + " миллисекунд");
        System.out.println("100 перцентиль: " +  decimalFormat.format(percentile.evaluate(responseTimes, 100)) + " миллисекунд");
    }

    private static void interruptThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                System.out.println("Сигнал остановки передан потоку " + thread.getName());
                thread.interrupt();
            }
        }
    }

    private static String getUrl() {
        return url;
    }
    private static void setUrl(String url) {
        BenchmarkTool.url = url;
    }
    private static long getNum() {
        return num;
    }
    private static void setNum(long num) {
        BenchmarkTool.num = num;
    }
    private static long getConcurrency() {
        return concurrency;
    }
    private static void setConcurrency(long concurrency) {
        BenchmarkTool.concurrency = concurrency;
    }
    private static long getTimeout() {
        return timeout;
    }
    private static void setTimeout(long timeout) {
        BenchmarkTool.timeout = timeout;
    }
}
