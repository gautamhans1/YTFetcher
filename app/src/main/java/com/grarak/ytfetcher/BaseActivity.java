package com.grarak.ytfetcher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.grarak.ytfetcher.utils.Settings;

import io.fabric.sdk.android.Fabric;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setTheme(Settings.isDarkTheme(this) ?
                R.style.AppThemeDark : R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
    }
}
