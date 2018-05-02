package com.grarak.ytfetcher.utils.server;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Request implements Closeable {

    public interface RequestCallback {
        void onConnect(Request request, int status, String url);

        void onSuccess(Request request, int status,
                       Map<String, List<String>> headers, String response);

        void onFailure(Request request, Exception e);
    }

    private HttpURLConnection connection;
    private Handler handler;
    private AtomicBoolean closed = new AtomicBoolean();

    Request() {
        handler = new Handler(Looper.getMainLooper());
    }

    void doRequest(String url, String contentType,
                   String data, RequestCallback requestCallback) {
        closed.set(false);
        BufferedReader reader = null;
        DataOutputStream outputStream = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(5000);
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
            if (data != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
            } else {
                connection.setRequestMethod("GET");
            }
            connection.connect();

            if (data != null) {
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(data);
                outputStream.flush();
            }

            int statusCode = connection.getResponseCode();
            switch (statusCode) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_SEE_OTHER:
                    String newUrl = connection.getHeaderField("Location");
                    if (newUrl == null) {
                        handler.post(() -> requestCallback.onFailure(this, null));
                    } else {
                        doRequest(newUrl, contentType, data, requestCallback);
                    }
                    return;
            }

            handler.post(() -> requestCallback.onConnect(this, statusCode, url));
            InputStream inputStream;
            if (statusCode < 200 || statusCode >= 300) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            handler.post(() -> requestCallback.onSuccess(this, statusCode,
                    connection.getHeaderFields(), response.toString()));
        } catch (IOException e) {
            if (!closed.get()) {
                handler.post(() -> requestCallback.onFailure(this, e));
            }
        } finally {
            connection.disconnect();

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void close() {
        closed.set(true);
        new Thread(() -> {
            if (connection != null) {
                connection.disconnect();
            }
        }).start();
    }
}
