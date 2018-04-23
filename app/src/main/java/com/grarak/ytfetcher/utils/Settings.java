package com.grarak.ytfetcher.utils;

import android.content.Context;
import android.support.annotation.NonNull;

public class Settings {

    public static String getLastSearch(Context context) {
        return Prefs.getString("last_search", "", context);
    }

    public static void setLastSearch(Context context, String search) {
        Prefs.saveString("last_search", search, context);
    }

    public static void setPage(Context context, int page) {
        Prefs.saveInt("page", page, context);
    }

    public static int getPage(Context context) {
        return Prefs.getInt("page", 0, context);
    }

    public static void setServerUrl(@NonNull String url, Context context) {
        Prefs.saveString("server_url", url, context);
    }

    @NonNull
    public static String getServerUrl(Context context) {
        return Prefs.getString("server_url", "", context);
    }
}
