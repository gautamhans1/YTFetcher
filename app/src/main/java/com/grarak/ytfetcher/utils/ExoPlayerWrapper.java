package com.grarak.ytfetcher.utils;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.grarak.ytfetcher.R;

public class ExoPlayerWrapper implements Player.EventListener {

    private enum State {
        PREPARING,
        PLAYING,
        PAUSED,
        IDLE
    }

    public interface OnPreparedListener {
        void onPrepared(ExoPlayerWrapper exoPlayer);
    }

    public interface OnCompletionListener {
        void onCompletion(ExoPlayerWrapper exoPlayer);
    }

    public interface OnErrorListener {
        void onError(ExoPlayerWrapper exoPlayer, ExoPlaybackException error);
    }

    private SimpleExoPlayer exoPlayer;
    private DataSource.Factory dataSourceFactory;

    private final Object stateLock = new Object();
    private State state;

    private OnPreparedListener onPreparedListener;
    private OnCompletionListener onCompletionListener;
    private OnErrorListener onErrorListener;

    public ExoPlayerWrapper(Context context) {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        exoPlayer.addListener(this);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)));
    }

    public void setDataSource(String url) {
        setState(State.PREPARING);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url));
        exoPlayer.prepare(mediaSource, true, true);
    }

    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public void seekTo(long position) {
        exoPlayer.seekTo(position);
    }

    public void setAudioAttributes(AudioAttributes audioAttributes) {
        exoPlayer.setAudioAttributes(audioAttributes);
    }

    public void setVolume(float volume) {
        exoPlayer.setVolume(volume);
    }

    public void play() {
        setState(State.PLAYING);
        exoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        setState(State.PAUSED);
        exoPlayer.setPlayWhenReady(false);
    }

    public boolean isPlaying() {
        return getState() == State.PLAYING;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.onPreparedListener = onPreparedListener;
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        this.onCompletionListener = onCompletionListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public boolean isPreparing() {
        return getState() == State.PREPARING;
    }

    public void release() {
        exoPlayer.release();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_READY:
                if (getState() == State.PREPARING && onPreparedListener != null) {
                    setState(State.IDLE);
                    onPreparedListener.onPrepared(this);
                }
                break;
            case Player.STATE_ENDED:
                setState(State.IDLE);
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion(this);
                }
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (onErrorListener != null) {
            onErrorListener.onError(this, error);
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }

    private void setState(State state) {
        synchronized (stateLock) {
            this.state = state;
        }
    }

    private State getState() {
        synchronized (stateLock) {
            return state;
        }
    }
}
