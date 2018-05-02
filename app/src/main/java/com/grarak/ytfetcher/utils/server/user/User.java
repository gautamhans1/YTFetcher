package com.grarak.ytfetcher.utils.server.user;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.grarak.ytfetcher.utils.Prefs;
import com.grarak.ytfetcher.utils.server.Gson;

public class User extends Gson {

    public String apikey;
    public String name;
    public String password;
    public boolean admin;
    public boolean verified;

    public void save(Context context) {
        Prefs.saveString("user", toString(), context);
    }

    public static User restore(Context context) {
        return fromString(Prefs.getString("user", null, context));
    }

    public static void delete(Context context) {
        Prefs.remove("user", context);
    }

    public static User fromString(String json) {
        try {
            return new GsonBuilder().create().fromJson(json, User.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
