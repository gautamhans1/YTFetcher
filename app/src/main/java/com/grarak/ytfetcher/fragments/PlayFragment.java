package com.grarak.ytfetcher.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grarak.ytfetcher.R;

public class PlayFragment extends TitleFragment {

    public interface PlayListener {
        void onPlay();

        void onShuffle();

        void onDownload();
    }

    private PlayListener playListener;

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

        rootView.findViewById(R.id.download).setOnClickListener(v -> {
            if (playListener != null) {
                playListener.onDownload();
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
}
