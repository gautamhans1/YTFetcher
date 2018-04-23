package com.grarak.ytfetcher.views.musicplayer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.MusicManager;
import com.grarak.ytfetcher.utils.Utils;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.concurrent.atomic.AtomicBoolean;

public class MusicPlayerView extends LinearLayout {

    private AppCompatImageView thumbnailView;
    private TextView titleView;
    private TextView positionTextView;
    private AppCompatSeekBar seekBar;
    private View previousView;
    private FloatingActionButton playPauseView;
    private View nextView;
    private View controls;
    private View progressView;

    private MusicManager musicManager;

    private Drawable playDrawable;
    private Drawable pauseDrawable;

    private long duration;
    private AtomicBoolean playing = new AtomicBoolean();

    public MusicPlayerView(Context context) {
        this(context, null);
    }

    public MusicPlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.view_music_player, this);

        thumbnailView = findViewById(R.id.thumbnail);
        titleView = findViewById(R.id.title);
        positionTextView = findViewById(R.id.position_text);
        seekBar = findViewById(R.id.seekbar);
        controls = findViewById(R.id.controls_view);
        previousView = findViewById(R.id.previous_btn);
        playPauseView = findViewById(R.id.play_pause_btn);
        nextView = findViewById(R.id.next_btn);
        progressView = findViewById(R.id.progress);

        playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play);
        pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause);

        playPauseView.setOnClickListener(v -> {
            if (playing.get()) {
                musicManager.pause();
            } else {
                musicManager.resume();
            }
        });

        previousView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        seekBar.setClickable(true);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                positionTextView.setText(
                        String.format(
                                "%s/%s",
                                Utils.formatSeconds(progress),
                                Utils.formatSeconds(duration)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopCounter();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicManager.seekTo(seekBar.getProgress() * 1000);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (playing.get()) {
            startCounter();
        }
    }

    private void startCounter() {
        if (getHandler() != null) {
            getHandler().postDelayed(counter, 500);
        }
    }

    private void stopCounter() {
        if (getHandler() != null) {
            getHandler().removeCallbacks(counter);
        }
    }

    private Runnable counter = new Runnable() {
        @Override
        public void run() {
            duration = musicManager.getDuration() / 1000;
            if (duration > 0) {
                long position = musicManager.getCurrentPosition() / 1000;
                positionTextView.setText(
                        String.format(
                                "%s/%s",
                                Utils.formatSeconds(position),
                                Utils.formatSeconds(duration)));
                seekBar.setMax((int) duration);
                seekBar.setProgress((int) position);
            }
            if (playing.get()) {
                startCounter();
            }
        }
    };

    void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    void onFetch(YoutubeSearchResult result) {
        stopCounter();
        Glide.with(this).load(result.thumbnail).into(thumbnailView);
        titleView.setText(result.title);
        controls.setVisibility(INVISIBLE);
        progressView.setVisibility(VISIBLE);
        positionTextView.setText("");
        seekBar.setProgress(0);
        playing.set(false);
    }

    void onFailure(YoutubeSearchResult result) {
        stopCounter();
        playing.set(false);
    }

    void onPlay(YoutubeSearchResult result) {
        Glide.with(this).load(result.thumbnail).into(thumbnailView);
        titleView.setText(result.title);
        controls.setVisibility(VISIBLE);
        progressView.setVisibility(INVISIBLE);
        playPauseView.setImageDrawable(pauseDrawable);
        playing.set(true);
        counter.run();
    }

    void onPause(YoutubeSearchResult result) {
        stopCounter();
        Glide.with(this).load(result.thumbnail).into(thumbnailView);
        titleView.setText(result.title);
        controls.setVisibility(VISIBLE);
        progressView.setVisibility(INVISIBLE);
        playPauseView.setImageDrawable(playDrawable);
        playing.set(false);
    }

    void onNoMusic() {
        stopCounter();
        playing.set(false);
    }
}
