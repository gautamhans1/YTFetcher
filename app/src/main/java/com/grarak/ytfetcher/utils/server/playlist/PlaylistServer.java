package com.grarak.ytfetcher.utils.server.playlist;

import android.content.Context;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Request;
import com.grarak.ytfetcher.utils.server.Server;
import com.grarak.ytfetcher.utils.server.Status;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class PlaylistServer extends Server {

    public interface PlaylistNameListCallback {
        void onSuccess(Playlists playlists);

        void onFailure(int code);
    }

    public interface PlayListLinksCallback {
        void onSuccess(List<String> links);

        void onFailure(int code);
    }

    public PlaylistServer(Context context) {
        super(Settings.getServerUrl(context));
    }

    public void list(String apiKey, PlaylistNameListCallback playlistNameListCallback) {
        post(getUrl("users/playlist/list"),
                String.format("{\"apikey\":\"%s\"}", apiKey), new Request.RequestCallback() {
                    @Override
                    public void onConnect(Request request, int status, String url) {
                    }

                    @Override
                    public void onSuccess(Request request, int status,
                                          Map<String, List<String>> headers, String response) {
                        if (status == HttpURLConnection.HTTP_OK) {
                            playlistNameListCallback.onSuccess(new Playlists(response));
                        } else {
                            playlistNameListCallback.onFailure(parseStatusCode(response));
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        playlistNameListCallback.onFailure(Status.ServerOffline);
                    }
                });
    }

    public void create(Playlist playlist, GenericCallback genericCallback) {
        post(getUrl("users/playlist/create"), playlist.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void setPublic(Playlist playlist, GenericCallback genericCallback) {
        post(getUrl("users/playlist/setpublic"), playlist.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void delete(Playlist playlist, GenericCallback genericCallback) {
        post(getUrl("users/playlist/delete"), playlist.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void addToPlaylist(PlaylistId playlistId, GenericCallback genericCallback) {
        post(getUrl("users/playlist/addid"), playlistId.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void deleteFromPlaylist(PlaylistId playlistId, GenericCallback genericCallback) {
        post(getUrl("users/playlist/deleteid"), playlistId.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void listPlaylistIds(Playlist playlist, PlayListLinksCallback playListLinksCallback) {
        post(getUrl("users/playlist/listids"), playlist.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    Type listType = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> results = new GsonBuilder().create().fromJson(response, listType);
                    playListLinksCallback.onSuccess(results);
                } else {
                    playListLinksCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                playListLinksCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void setPlaylistIds(PlaylistIds playlistIds, GenericCallback genericCallback) {
        post(getUrl("users/playlist/setids"), playlistIds.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    genericCallback.onSuccess();
                } else {
                    genericCallback.onFailure(Status.ServerOffline);
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                genericCallback.onFailure(Status.ServerOffline);
            }
        });
    }
}
