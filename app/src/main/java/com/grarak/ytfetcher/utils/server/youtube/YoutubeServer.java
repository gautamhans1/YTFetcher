package com.grarak.ytfetcher.utils.server.youtube;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.server.Request;
import com.grarak.ytfetcher.utils.server.Server;
import com.grarak.ytfetcher.utils.server.Status;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

public class YoutubeServer extends Server {

    public interface YoutubeSongIdCallback {
        void onSuccess(String url);

        void onFailure(int code);
    }

    public interface YoutubeChartsCallback {
        void onSuccess(YoutubeCharts youtubeCharts);

        void onFailure(int code);
    }

    public interface YoutubeResultsCallback {
        void onSuccess(List<YoutubeSearchResult> youtubeSearchResults);

        void onFailure(int code);
    }

    public YoutubeServer(Context context) {
        super(Settings.getServerUrl(context));
    }

    public void fetchSong(Youtube youtube, YoutubeSongIdCallback youtubeSongIdCallback) {
        post(getUrl("youtube/fetch"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    youtubeSongIdCallback.onSuccess(response);
                } else {
                    youtubeSongIdCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                youtubeSongIdCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void search(Youtube youtube, YoutubeResultsCallback youtubeResultsCallback) {
        post(getUrl("youtube/search"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    try {
                        Type listType = new TypeToken<List<YoutubeSearchResult>>() {
                        }.getType();
                        List<YoutubeSearchResult> results =
                                new GsonBuilder().create().fromJson(response, listType);
                        youtubeResultsCallback.onSuccess(results);
                    } catch (Exception ignored) {
                        youtubeResultsCallback.onFailure(Status.ServerOffline);
                    }
                } else {
                    youtubeResultsCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                youtubeResultsCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void getCharts(Youtube youtube, YoutubeChartsCallback youtubeChartsCallback) {
        post(getUrl("youtube/getcharts"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    YoutubeCharts youtubeCharts = new YoutubeCharts(response);
                    if (youtubeCharts.items.size() > 0) {
                        youtubeChartsCallback.onSuccess(youtubeCharts);
                    } else {
                        youtubeChartsCallback.onFailure(Status.ServerOffline);
                    }
                } else {
                    youtubeChartsCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                youtubeChartsCallback.onFailure(Status.ServerOffline);
            }
        });
    }
}
