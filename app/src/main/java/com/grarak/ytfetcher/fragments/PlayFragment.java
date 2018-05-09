package com.grarak.ytfetcher.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.ytfetcher.R;

public class PlayFragment extends TitleFragment {

    public interface PlayListener {
        void onPlay();

        void onShuffle();

        void onDownload();

        void onSave();
    }

    private PlayListener playListener;
    private boolean readyOnly;
    private FloatingActionButton downloadButton;
    private Drawable downloadDrawable;
    private Drawable saveDrawable;

    @Override
    protected int getLayoutXml() {
        return R.layout.fragment_play;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        downloadButton = rootView.findViewById(R.id.download);
        setDownloadButton();
        downloadButton.setOnClickListener(v -> {
            if (playListener != null) {
                if (readyOnly) {
                    playListener.onSave();
                } else {
                    playListener.onDownload();
                }
            }
        });
        rootView.findViewById(R.id.shuffle).setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onShuffle();
            }
        });
        rootView.findViewById(R.id.play).setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onPlay();
            }
        });

        return rootView;
    }

    public void setPlayListener(PlayListener playListener) {
        this.playListener = playListener;
    }

    private void setDownloadButton() {
        if (downloadButton != null) {
            if (saveDrawable == null) {
                saveDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_save);
                DrawableCompat.setTint(saveDrawable, Color.WHITE);
            }

            if (downloadDrawable == null) {
                downloadDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_download);
                DrawableCompat.setTint(downloadDrawable, Color.WHITE);
            }
            downloadButton.setImageDrawable(readyOnly ? saveDrawable : downloadDrawable);
        }
    }

    public void setReadOnly(boolean readyOnly) {
        this.readyOnly = readyOnly;
        setDownloadButton();
    }
}
