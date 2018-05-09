package com.grarak.ytfetcher.utils.server.youtube;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.grarak.ytfetcher.utils.Prefs;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.Gson;

import java.io.File;

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

    public File getDownloadPath(Context context) {
        return new File(Utils.getDownloadFolder(context) + "/" + id + ".ogg");
    }

    public void save(Context context) {
        Prefs.saveString("result_" + id, toString(), context);
    }

    public static YoutubeSearchResult restore(String id, Context context) {
        return fromString(Prefs.getString("result_" + id, null, context));
    }

    public boolean delete(Context context) {
        if (getDownloadPath(context).delete()) {
            Prefs.remove("result_" + id, context);
            return true;
        }
        return false;
    }
}
