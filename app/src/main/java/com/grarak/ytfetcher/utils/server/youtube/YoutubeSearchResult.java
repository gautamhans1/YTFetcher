package com.grarak.ytfetcher.utils.server.youtube;

import com.google.gson.GsonBuilder;
import com.grarak.ytfetcher.utils.server.Gson;

public class YoutubeSearchResult extends Gson {

    public String title;
    public String id;
    public String thumbnail;
    public String duration;

    public static YoutubeSearchResult fromString(String json) {
        try {
            return new GsonBuilder().create().fromJson(json, YoutubeSearchResult.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
