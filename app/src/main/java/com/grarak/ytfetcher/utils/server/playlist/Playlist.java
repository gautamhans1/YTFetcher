package com.grarak.ytfetcher.utils.server.playlist;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.grarak.ytfetcher.utils.server.Gson;

public class Playlist extends Gson {

    public String apikey;
    public String name;
    @SerializedName("public")
    public boolean isPublic;

    public static Playlist fromString(String json) {
        try {
            return new GsonBuilder().create().fromJson(json, Playlist.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
