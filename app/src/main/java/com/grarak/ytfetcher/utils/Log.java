package com.grarak.ytfetcher.utils;

public class Log {

    private static final String TAG = "YTFetcher";

    public static void i(String message) {
        android.util.Log.i(TAG, getMessage(message));
    }

    private static String getMessage(String message) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        String className = element.getClassName();

        return String.format("[%s][%s] %s",
                className.substring(className.lastIndexOf(".") + 1),
                element.getMethodName(),
                message);
    }
}
