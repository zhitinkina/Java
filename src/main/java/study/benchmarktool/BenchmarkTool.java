package study.benchmarktool;

import org.apache.commons.cli.Options;

public class BenchmarkTool {

    private static String url;
    private static long num;
    private static long concurrency;
    private static long timeout = 30000;

    public static void main(String[] args) throws Exception {
        setOptions(args);
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
    }
}
