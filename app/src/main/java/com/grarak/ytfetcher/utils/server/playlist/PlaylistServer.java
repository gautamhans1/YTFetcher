package com.grarak.ytfetcher.utils.server.playlist;

import android.content.Context;

import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.server.GenericCallback;
import com.grarak.ytfetcher.utils.server.Request;
import com.grarak.ytfetcher.utils.server.Server;
import com.grarak.ytfetcher.utils.server.Status;

import java.net.HttpURLConnection;

public class PlaylistServer extends Server {

    public interface PlaylistNameListCallback {
        void onSuccess(PlaylistNamesList playlistNamesList);

        void onFailure(int code);
    }

    public PlaylistServer(Context context) {
        super(Settings.getServerUrl(context));
    }

    public void listNames(String apiKey, PlaylistNameListCallback playlistNameListCallback) {
        post(getUrl("users/playlist/list"),
                String.format("{\"apikey\":\"%s\"}", apiKey),
                new Request.RequestCallback() {
                    @Override
                    public void onSuccess(Request request, int status, String response) {
                        if (status == HttpURLConnection.HTTP_OK) {
                            playlistNameListCallback.onSuccess(new PlaylistNamesList(response));
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

    public void createName(PlaylistName playlistName, GenericCallback genericCallback) {
        post(getUrl("users/playlist/create"), playlistName.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
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

    public void setPublic(PlaylistName playlistName, GenericCallback genericCallback) {
        post(getUrl("users/playlist/setpublic"), playlistName.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
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
}
