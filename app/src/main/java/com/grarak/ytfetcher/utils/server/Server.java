package com.grarak.ytfetcher.utils.server;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class Server implements Closeable {

    private static final String API_VERSION = "v1";

    private String url;
    private List<Request> requests = new ArrayList<>();

    public Server(String url) {
        this.url = url;
    }

    protected int parseStatusCode(String response) {
        Integer statusCode = Status.getStatusCode(response);
        return statusCode == null ? Status.ServerOffline : statusCode;
    }

    protected String getUrl(String path) {
        return url + "/api/" + API_VERSION + "/" + path;
    }

    protected void get(String url, Request.RequestCallback requestCallback) {
        Request request = new Request();
        synchronized (this) {
            requests.add(request);
        }
        new Thread(() -> request.doRequest(url,
                null, null, new Request.RequestCallback() {
                    @Override
                    public void onSuccess(Request request, int status, String response) {
                        handleRequestCallbackSuccess(requestCallback, request, status, response);
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        handleRequestCallbackFailure(requestCallback, request, e);
                    }
                })).start();
    }

    protected void post(String url, String data, Request.RequestCallback requestCallback) {
        Request request = new Request();
        synchronized (this) {
            requests.add(request);
        }
        new Thread(() -> request.doRequest(url, "application/json",
                data, new Request.RequestCallback() {
                    @Override
                    public void onSuccess(Request request, int status, String response) {
                        handleRequestCallbackSuccess(requestCallback, request, status, response);
                    }

                    @Override
                    public void onFailure(Request request, Exception e) {
                        handleRequestCallbackFailure(requestCallback, request, e);
                    }
                })).start();
    }

    private void handleRequestCallbackSuccess(Request.RequestCallback requestCallback,
                                              Request request, int status, String response) {
        synchronized (this) {
            requests.remove(request);
        }
        requestCallback.onSuccess(request, status, response);
    }

    private void handleRequestCallbackFailure(Request.RequestCallback requestCallback,
                                              Request request, Exception e) {
        synchronized (this) {
            requests.remove(request);
        }
        requestCallback.onFailure(request, e);
    }

    @Override
    public void close() {
        for (Request request : requests) {
            request.close();
        }
        requests.clear();
    }

}
