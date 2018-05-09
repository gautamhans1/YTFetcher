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
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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

    public interface YoutubeResultCallback {
        void onSuccess(YoutubeSearchResult result);

        void onFailure(int code);
    }

    private final Context context;

    public YoutubeServer(Context context) {
        super(Settings.getServerUrl(context));
        this.context = context;
    }

    public void fetchSong(Youtube youtube, YoutubeSongIdCallback youtubeSongIdCallback) {
        post(getUrl("youtube/fetch"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    if (headers.containsKey("ytfetcher-id")) {
                        verifyFetchedSong(response.trim(),
                                headers.get("ytfetcher-id").get(0), youtubeSongIdCallback);
                    } else {
                        youtubeSongIdCallback.onSuccess(response);
                    }
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

    private void verifyFetchedSong(String url, String id, YoutubeSongIdCallback youtubeSongIdCallback) {
        get(url, new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
                request.close();
                if (status >= 200 && status < 300) {
                    youtubeSongIdCallback.onSuccess(url);
                } else {
                    verifyForwardedSong();
                }
            }

            @Override
            public void onSuccess(Request request, int status, Map<String, List<String>> headers, String response) {
            }

            @Override
            public void onFailure(Request request, Exception e) {
                verifyForwardedSong();
            }

            private void verifyForwardedSong() {
                get(getNewUrl(), new Request.RequestCallback() {
                    @Override
                    public void onConnect(Request request, int status, String url) {
                        request.close();
                        youtubeSongIdCallback.onSuccess(url);
                    }

                    @Override
                    public void onSuccess(Request request, int status, Map<String, List<String>> headers, String response) {
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        youtubeSongIdCallback.onFailure(Status.ServerOffline);
                    }
                });
            }

            private String getNewUrl() {
                return getUrl("youtube/get?id=")
                        + URLEncoder.encode(id)
                        + "&url=" + URLEncoder.encode(url);
            }
        });
    }

    public void search(Youtube youtube, YoutubeResultsCallback youtubeResultsCallback) {
        post(getUrl("youtube/search"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    Type listType = new TypeToken<List<YoutubeSearchResult>>() {
                    }.getType();
                    List<YoutubeSearchResult> results =
                            new GsonBuilder().create().fromJson(response, listType);
                    if (results != null) {
                        youtubeResultsCallback.onSuccess(results);
                    } else {
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
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
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

    public void getInfo(Youtube youtube, YoutubeResultCallback youtubeResultCallback) {
        YoutubeSearchResult result = YoutubeSearchResult.restore(youtube.id, context);
        if (result != null) {
            youtubeResultCallback.onSuccess(result);
            return;
        }

        post(getUrl("youtube/getinfo"), youtube.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    YoutubeSearchResult result = YoutubeSearchResult.fromString(response);
                    if (result != null) {
                        youtubeResultCallback.onSuccess(result);
                    } else {
                        youtubeResultCallback.onFailure(Status.ServerOffline);
                    }
                } else {
                    youtubeResultCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                youtubeResultCallback.onFailure(Status.ServerOffline);
            }
        });
    }
}
