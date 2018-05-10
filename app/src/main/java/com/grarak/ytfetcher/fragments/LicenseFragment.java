package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class LicenseFragment extends BaseFragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        webView = new WebView(getActivity());
        webView.loadUrl("file:///android_asset/licenses.html");

        if (savedInstanceState != null) {
            webView.scrollTo(savedInstanceState.getInt("scrollX"),
                    savedInstanceState.getInt("scrollY"));
        }

        return webView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollX", webView.getScrollX());
        outState.putInt("scrollY", webView.getScrollY());
    }
}
