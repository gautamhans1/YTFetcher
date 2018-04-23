package com.grarak.ytfetcher.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.StringRes;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Utils {

    public static String formatSeconds(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        String format = "";
        if (minutes < 10) {
            format += "0";
        }
        format += minutes + ":";
        if (seconds < 10) {
            format += "0";
        }
        format += seconds;
        return format;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    public static String encode(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    }

    public static void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void toast(@StringRes int message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
