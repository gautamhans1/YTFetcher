package com.grarak.ytfetcher.utils.server.youtube;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.grarak.ytfetcher.utils.Prefs;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YoutubeCharts implements Iterable<YoutubeSearchResult> {

    public List<YoutubeSearchResult> items = new ArrayList<>();

    YoutubeCharts(String json) {
        try {
            Type listType = new TypeToken<List<YoutubeSearchResult>>() {
            }.getType();
            items.addAll(new GsonBuilder().create().fromJson(json, listType));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(items);
    }

    public void save(Context context) {
        Prefs.saveString("youtube_charts", toString(), context);
    }

    public static YoutubeCharts restore(Context context) {
        return new YoutubeCharts(Prefs.getString("youtube_charts", null, context));
    }

    @NonNull
    @Override
    public Iterator<YoutubeSearchResult> iterator() {
        return items.iterator();
    }
}
