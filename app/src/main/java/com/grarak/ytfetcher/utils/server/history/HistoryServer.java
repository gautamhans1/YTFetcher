package com.grarak.ytfetcher.utils.server.history;

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

public class HistoryServer extends Server {

    public interface HistoryCallback {
        void onSuccess(List<String> history);

        void onFailure(int code);
    }

    public HistoryServer(Context context) {
        super(Settings.getServerUrl(context));
    }

    public void add(History history, GenericCallback genericCallback) {
        post(getUrl("users/history/add"), history.toString(), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status, Map<String, List<String>> headers, String response) {
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

    public void get(String apiKey, HistoryCallback historyCallback) {
        post(getUrl("users/history/list"), String.format("{\"apikey\":\"%s\"}", apiKey), new Request.RequestCallback() {
            @Override
            public void onConnect(Request request, int status, String url) {
            }

            @Override
            public void onSuccess(Request request, int status, Map<String, List<String>> headers, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    Type listType = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> results = new GsonBuilder().create().fromJson(response, listType);
                    historyCallback.onSuccess(results);
                } else {
                    historyCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                historyCallback.onFailure(Status.ServerOffline);
            }
        });
    }


}
