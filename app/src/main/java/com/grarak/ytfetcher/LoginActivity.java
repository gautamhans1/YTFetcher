package com.grarak.ytfetcher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.Button;

import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.user.UserServer;
import com.grarak.ytfetcher.views.EditTextView;

public class LoginActivity extends BaseActivity {

    private UserServer server;

    private EditTextView serverView;
    private EditTextView usernameView;
    private EditTextView passwordView;
    private EditTextView confirmPasswordView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = User.restore(this);
        if (user != null && user.verified) {
            launchMainActivity(user);
            return;
        }

        setContentView(R.layout.activity_login);

        serverView = findViewById(R.id.server_edit);
        serverView.getTextInputEditText().setInputType(
                InputType.TYPE_TEXT_VARIATION_FILTER);
        usernameView = findViewById(R.id.username_edit);
        usernameView.getTextInputEditText().setInputType(
                InputType.TYPE_TEXT_VARIATION_FILTER);
        passwordView = findViewById(R.id.password_edit);
        passwordView.getTextInputEditText().setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordView = findViewById(R.id.confirm_password_edit);
        confirmPasswordView.getTextInputEditText().setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        if (savedInstanceState == null) {
            String url = Settings.getServerUrl(this);
            if (!url.isEmpty()) {
                serverView.setText(url);
                usernameView.requestFocus();
            }
        } else {
            serverView.setText(savedInstanceState.getString("server_url"));
            usernameView.setText(savedInstanceState.getString("username"));
            passwordView.setText(savedInstanceState.getString("password"));
            confirmPasswordView.setText(savedInstanceState.getString("confirm_password"));
            if (savedInstanceState.getBoolean("progress")) {
                showProgress();
            }
        }

        findViewById(R.id.done_btn).setOnClickListener(v -> {
            String serverText = serverView.getText().toString();
            if (serverText.isEmpty()) {
                Utils.toast(R.string.server_empty, this);
                return;
            } else if (!serverText.startsWith("http")) {
                serverText = "http://" + serverText;
                serverView.setText(serverText);
            }

            if (usernameView.getText().length() <= 3) {
                Utils.toast(R.string.username_short, this);
                return;
            } else if (!usernameView.getText().toString().matches("^[a-zA-Z0-9_]*$")) {
                Utils.toast(R.string.username_error, this);
                return;
            }

            if (passwordView.getText().length() <= 4) {
                Utils.toast(R.string.password_short, this);
                return;
            }

            boolean signup = confirmPasswordView.getVisibility() == View.VISIBLE;
            if (signup && !confirmPasswordView.getText()
                    .toString().equals(passwordView.getText().toString())) {
                Utils.toast(R.string.password_no_match, this);
                return;
            }

            Settings.setServerUrl(serverText, LoginActivity.this);
            server = new UserServer(serverText);
            User postUser = new User();
            postUser.name = usernameView.getText().toString();
            postUser.password = Utils.encode(passwordView.getText().toString());

            UserServer.UserCallback userCallback = new UserServer.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    hideProgress();

                    if (!user.verified) {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setMessage(R.string.not_verified)
                                .setPositiveButton(R.string.ok, null).show();
                    } else {
                        user.save(LoginActivity.this);
                        launchMainActivity(user);
                    }
                }

                @Override
                public void onFailure(int code) {
                    hideProgress();
                    if (code == Status.UserAlreadyExists) {
                        Utils.toast(R.string.username_exists, LoginActivity.this);
                    } else if (code == Status.InvalidPassword) {
                        Utils.toast(R.string.username_password_wrong, LoginActivity.this);
                    } else {
                        Utils.toast(R.string.server_offline, LoginActivity.this);
                    }
                }
            };

            showProgress();
            if (signup) {
                server.signUp(postUser, userCallback);
            } else {
                server.login(postUser, userCallback);
            }
        });

        Button switchBtn = findViewById(R.id.switch_btn);
        switchBtn.setOnClickListener(v -> {
            confirmPasswordView.setVisibility(confirmPasswordView.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE);
            switchBtn.setText(confirmPasswordView.getVisibility() == View.VISIBLE ?
                    R.string.switch_login : R.string.switch_signup);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (serverView != null) {
            outState.putString("server_url", serverView.getText().toString());
            outState.putString("username", usernameView.getText().toString());
            outState.putString("password", passwordView.getText().toString());
            outState.putString("confirm_password", confirmPasswordView.getText().toString());
            outState.putBoolean("progress", progressDialog != null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (server != null) {
            server.close();
        }
        hideProgress();
    }

    private void launchMainActivity(User user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.USER_INTENT, user);
        startActivity(intent);
        finish();
    }

    private void showProgress() {
        if (getCurrentFocus() != null) {
            Utils.hideKeyboard(getCurrentFocus());
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
