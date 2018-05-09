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

    public interface PlaylistListCallback {
        void onSuccess(Playlists playlists);

        void onFailure(int code);
    }

    public interface PlayListIdsCallback {
        void onSuccess(List<String> ids);

        void onFailure(int code);
    }

    public PlaylistServer(Context context) {
        super(Settings.getServerUrl(context));
    }

    public void list(String apiKey, PlaylistListCallback playlistListCallback) {
        post(getUrl("users/playlist/list"),
                String.format("{\"apikey\":\"%s\"}", apiKey), new Request.RequestCallback() {
                    @Override
                    public void onConnect(Request request, int status, String url) {
                    }

                    @Override
                    public void onSuccess(Request request, int status,
                                          Map<String, List<String>> headers, String response) {
                        if (status == HttpURLConnection.HTTP_OK) {
                            playlistListCallback.onSuccess(new Playlists(response));
                        } else {
                            playlistListCallback.onFailure(parseStatusCode(response));
                        }
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        playlistListCallback.onFailure(Status.ServerOffline);
                    }
                });
    }

    public void listPublic(Playlist playlist, PlaylistListCallback playlistListCallback) {
        post(getUrl("users/playlist/listpublic"), playlist.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status,
                                  Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    playlistListCallback.onSuccess(new Playlists(response));
                } else {
                    playlistListCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                playlistListCallback.onFailure(Status.ServerOffline);
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

    public void listPlaylistIds(Playlist playlist, PlayListIdsCallback playListIdsCallback) {
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
                    playListIdsCallback.onSuccess(results);
                } else {
                    playListIdsCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                playListIdsCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void listPlaylistIdsPublic(PlaylistPublic playlistPublic, PlayListIdsCallback playListIdsCallback) {
        post(getUrl("users/playlist/listidspublic"), playlistPublic.toString(), new Request.RequestCallback() {
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
                    playListIdsCallback.onSuccess(results);
                } else {
                    playListIdsCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                playListIdsCallback.onFailure(Status.ServerOffline);
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
