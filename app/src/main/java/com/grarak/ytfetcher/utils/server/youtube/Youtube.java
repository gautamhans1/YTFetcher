package com.grarak.ytfetcher.utils.server.youtube;

import com.google.gson.GsonBuilder;
import com.grarak.ytfetcher.utils.server.Gson;

public class Youtube extends Gson {

    public String apikey;
    public String searchquery;
    public String id;
    public boolean addhistory;

    public static Youtube fromString(String json) {
        try {
            return new GsonBuilder().create().fromJson(json, Youtube.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
