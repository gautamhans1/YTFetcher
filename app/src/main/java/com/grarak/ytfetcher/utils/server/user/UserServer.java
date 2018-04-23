package com.grarak.ytfetcher.utils.server.user;

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

public class UserServer extends Server {

    public interface UserCallback {
        void onSuccess(User user);

        void onFailure(int code);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);

        void onFailure(int code);
    }

    public UserServer(Context context) {
        this(Settings.getServerUrl(context));
    }

    public UserServer(String url) {
        super(url);
    }

    public void signUp(User user, UserCallback userCallback) {
        post(getUrl("users/signup"), user.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                handleUserCallbackSuccess(userCallback, status, response);
            }

            @Override
            public void onFailure(Request request, Exception e) {
                handleUserCallbackFailure(userCallback);
            }
        });
    }

    public void login(User user, UserCallback userCallback) {
        post(getUrl("users/login"), user.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                handleUserCallbackSuccess(userCallback, status, response);
            }

            @Override
            public void onFailure(Request request, Exception e) {
                handleUserCallbackFailure(userCallback);
            }
        });
    }

    public void list(User user, int page, UsersCallback usersCallback) {
        post(getUrl("users/list?page=" + page), user.toString(), new Request.RequestCallback() {
            @Override
            public void onSuccess(Request request, int status, String response) {
                if (status == HttpURLConnection.HTTP_OK) {
                    Type listType = new TypeToken<List<User>>() {
                    }.getType();
                    List<User> results =
                            new GsonBuilder().create().fromJson(response, listType);
                    usersCallback.onSuccess(results);
                } else {
                    usersCallback.onFailure(parseStatusCode(response));
                }
            }

            @Override
            public void onFailure(Request request, Exception e) {
                usersCallback.onFailure(Status.ServerOffline);
            }
        });
    }

    public void setVerification(User user, GenericCallback genericCallback) {
        post(getUrl("users/setverification"), user.toString(), new Request.RequestCallback() {
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

    private void handleUserCallbackSuccess(UserCallback userCallback,
                                           int status, String response) {
        if (status == HttpURLConnection.HTTP_OK) {
            User user = User.fromString(response);
            if (user != null) {
                userCallback.onSuccess(user);
            } else {
                userCallback.onFailure(Status.ServerOffline);
            }
        } else {
            userCallback.onFailure(parseStatusCode(response));
        }
    }

    private void handleUserCallbackFailure(UserCallback userCallback) {
        userCallback.onFailure(Status.ServerOffline);
    }
}
