package com.grarak.ytfetcher.views.musicplayer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.MusicManager;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.List;

public class MusicPlayerHeaderView extends LinearLayout {

    private MusicManager musicManager;

    private TextView titleView;
    private AppCompatImageView playPauseView;
    private View progressView;
    private AppCompatImageView thumbnailView;

    private Drawable playDrawable;
    private Drawable pauseDrawable;

    private boolean playing;

    public MusicPlayerHeaderView(Context context) {
        this(context, null);
    }

    public MusicPlayerHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicPlayerHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.view_music_player_header, this);

        titleView = findViewById(R.id.title);
        playPauseView = findViewById(R.id.play_pause_btn);
        progressView = findViewById(R.id.progress);
        thumbnailView = findViewById(R.id.thumbnail);

        playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play);
        pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause);

        playPauseView.setOnClickListener(v -> {
            if (playing) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
        });
    }

    void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    void onFetch(List<YoutubeSearchResult> results, int position) {
        onPlay(results, position);
        playPauseView.setVisibility(INVISIBLE);
        progressView.setVisibility(VISIBLE);
    }

    void onFailure(List<YoutubeSearchResult> results, int position) {
        onNoMusic();
    }

    void onPlay(List<YoutubeSearchResult> results, int position) {
        playing = true;
        playPauseView.setImageDrawable(pauseDrawable);
        playPauseView.setVisibility(VISIBLE);
        progressView.setVisibility(INVISIBLE);
        titleView.setText(results.get(position).title);
        thumbnailView.setVisibility(VISIBLE);
        Glide.with(this).load(results.get(position).thumbnail).into(thumbnailView);
    }

    void onPause(List<YoutubeSearchResult> results, int position) {
        onPlay(results, position);
        playing = false;
        playPauseView.setImageDrawable(playDrawable);
    }

    void onNoMusic() {
        titleView.setText(getResources().getString(R.string.no_music));
        thumbnailView.setVisibility(INVISIBLE);
        playPauseView.setVisibility(INVISIBLE);
        progressView.setVisibility(INVISIBLE);
    }
}
