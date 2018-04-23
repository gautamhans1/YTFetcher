package com.grarak.ytfetcher.utils.server.playlist;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.grarak.ytfetcher.utils.Prefs;
import com.grarak.ytfetcher.utils.server.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlaylistNamesList extends Gson implements Iterable<PlaylistName> {

    public List<PlaylistName> items = new ArrayList<>();

    PlaylistNamesList(String json) {
        try {
            Type listType = new TypeToken<List<PlaylistName>>() {
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
        Prefs.saveString("playlists", toString(), context);
    }

    public static PlaylistNamesList restore(Context context) {
        return new PlaylistNamesList(Prefs.getString("playlists", null, context));
    }

    @NonNull
    @Override
    public Iterator<PlaylistName> iterator() {
        return items.iterator();
    }
}
