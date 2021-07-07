package cn.itcast.job.utils;

public class Log {
    public static void i(String log) {
        System.out.println(log);
    }

    public static void t(String log) {
        System.out.printf("%s\t", log);
    }

    public static void d(String log) {
        System.out.println(log);
    }

    public static void error(String text, String... items) {
        System.out.printf(text, items);
        i("");
    }

    public static void error(String text, Exception e) {
        System.out.printf(text, e);
        i("");
    }

    public static void debug(String text, String... items) {
        System.out.printf(text, items);
        i("");
    }

    public static void info(String text) {
        i(text);
        i("");
    }

    public static void i(String tag, String s) {
        System.out.println(tag + ":" + s);
    }
}
