package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grarak.ytfetcher.R;

public class TitleFragment extends BaseFragment {

    private TextView titleView;
    private CharSequence title;

    @LayoutRes
    protected int getLayoutXml() {
        return R.layout.fragment_title;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutXml(), container, false);
        titleView = rootView.findViewById(R.id.title);
        if (titleView != null) {
            titleView.setText(title);
        }
        return rootView;
    }

    public void setText(CharSequence text) {
        title = text;
        if (titleView != null) {
            titleView.setText(title);
        }
    }
}
