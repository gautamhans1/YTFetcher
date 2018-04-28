package com.grarak.ytfetcher.utils.server;

import com.google.gson.JsonParser;

public class Status {
    public static final int ServerOffline = -1;
    public static final int NoError = 0;
    public static final int Invalid = 1;
    public static final int NameShort = 2;
    public static final int PasswordShort = 3;
    public static final int PasswordInvalid = 4;
    public static final int NameInvalid = 5;
    public static final int AddUserFailed = 6;
    public static final int UserAlreadyExists = 7;
    public static final int InvalidPassword = 8;
    public static final int PasswordLong = 9;
    public static final int NameLong = 10;
    public static final int YoutubeFetchFailure = 11;
    public static final int YoutubeSearchFailure = 12;
    public static final int YoutubeGetFailure = 13;
    public static final int YoutubeGetInfoFailure = 14;
    public static final int YoutubeGetChartsFailure = 15;
    public static final int PlaylistIdAlreadyExists = 16;
    public static final int AddHistoryFailed = 17;

    public static Integer getStatusCode(String json) {
        try {
            return new JsonParser().parse(json).getAsJsonObject().get("statuscode").getAsInt();
        } catch (Exception ignored) {
            return null;
        }
    }
}
