package com.grarak.ytfetcher.utils.server.playlist;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.grarak.ytfetcher.utils.Prefs;
import com.grarak.ytfetcher.utils.server.Gson;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class PlaylistResults extends Gson {

    public String name;
    public List<YoutubeSearchResult> songs;

    public void save(Context context) {
        Prefs.saveString("playlist_" + name, toString(), context);
    }

    public static PlaylistResults restore(String name, Context context) {
        return new GsonBuilder().create().fromJson(
                Prefs.getString("playlist_" + name,
                        null, context), PlaylistResults.class);
    }
}
