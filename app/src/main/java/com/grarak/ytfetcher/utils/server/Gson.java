package com.grarak.ytfetcher.utils.server;

import com.google.gson.GsonBuilder;

import java.io.Serializable;

public abstract class Gson implements Serializable {

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
