package com.grarak.ytfetcher.utils.server;

public interface GenericCallback {
    void onSuccess();

    void onFailure(int code);
}
