package com.grarak.ytfetcher.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.grarak.ytfetcher.utils.ExoPlayerWrapper;
import com.grarak.ytfetcher.utils.server.Status;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicPlayerService extends Service
        implements AudioManager.OnAudioFocusChangeListener,
        ExoPlayerWrapper.OnCompletionListener, ExoPlayerWrapper.OnErrorListener {

    private static final String NAME = MusicPlayerService.class.getName();
    public static final String ACTION_MUSIC_PLAYER_STOP = NAME + ".ACTION.MUSIC_PLAYER_STOP";
    public static final String ACTION_MUSIC_PLAY_PAUSE = NAME + ".ACTION.MUSIC_PLAY_PAUSE";
    public static final String ACTION_MUSIC_PREVIOUS = NAME + ".ACTION.MUSIC_PREVIOUS";
    public static final String ACTION_MUSIC_NEXT = NAME + ".ACTION.MUSIC_NEXT";

    private boolean isBound;
    private MusicPlayerBinder binder = new MusicPlayerBinder();

    private YoutubeServer server;
    private ExoPlayerWrapper exoPlayer;
    private MusicPlayerNotification notification;
    private MusicPlayerListener listener;

    private AudioFocusRequest audioFocusRequest;

    private final Object focusLock = new Object();
    private boolean playbackDelayed;
    private boolean resumeOnFocusGain;

    private final Object trackLock = new Object();
    private List<YoutubeSearchResult> tracks = new ArrayList<>();
    private User user;
    private int currentTrackPosition = -1;
    private int preparingTrackPositon = -1;
    private long lastMusicPosition;

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_MUSIC_PLAYER_STOP)) {
                if (!isBound()) {
                    stopSelf();
                }
            } else if (intent.getAction().equals(ACTION_MUSIC_PLAY_PAUSE)) {
                if (isPlaying()) {
                    pauseMusic();
                } else {
                    requestAudioFocus();
                }
            } else if (intent.getAction().equals(ACTION_MUSIC_PREVIOUS)) {
                boolean play = false;
                synchronized (trackLock) {
                    if (currentTrackPosition - 1 >= 0 && currentTrackPosition - 1 < tracks.size()) {
                        play = true;
                    }
                }
                if (play) {
                    playMusic(user, tracks, currentTrackPosition - 1);
                }
            } else if (intent.getAction().equals(ACTION_MUSIC_NEXT)) {
                boolean play = false;
                synchronized (trackLock) {
                    if (currentTrackPosition != -1 && currentTrackPosition + 1 < tracks.size()) {
                        play = true;
                    }
                }
                if (play) {
                    playMusic(user, tracks, currentTrackPosition + 1);
                }
            } else if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pauseMusic();
            }
        }
    };

    public void playMusic(User user, YoutubeSearchResult result) {
        playMusic(user, Collections.singletonList(result), 0);
    }

    public synchronized void playMusic(User user, List<YoutubeSearchResult> results, int position) {
        pauseMusic();
        server.close();

        synchronized (trackLock) {
            this.user = user;
            currentTrackPosition = -1;
            preparingTrackPositon = position;
            lastMusicPosition = 0;
            if (tracks != results) {
                tracks.clear();
                tracks.addAll(results);
            }
        }

        if (listener != null) {
            listener.onFetchingSong(tracks, position);
        }

        YoutubeSearchResult result = tracks.get(position);
        notification.showProgress(result);

        File file = result.getDownloadPath(this);
        if (file.exists()) {
            exoPlayer.setFile(file);
            exoPlayer.setOnPreparedListener(exoPlayer -> {
                synchronized (trackLock) {
                    preparingTrackPositon = -1;
                    currentTrackPosition = position;
                    requestAudioFocus();
                }
            });
            return;
        }

        Youtube youtube = new Youtube();
        youtube.apikey = user.apikey;
        youtube.id = result.id;
        youtube.addhistory = true;
        server.fetchSong(youtube, new YoutubeServer.YoutubeSongIdCallback() {
            @Override
            public void onSuccess(String url) {
                exoPlayer.setUrl(url);
                exoPlayer.setOnPreparedListener(exoPlayer -> {
                    synchronized (trackLock) {
                        preparingTrackPositon = -1;
                        currentTrackPosition = position;
                        requestAudioFocus();
                    }
                });
            }

            @Override
            public void onFailure(int code) {
                if (listener != null) {
                    listener.onFailure(code, results, position);
                }
                boolean play;
                synchronized (trackLock) {
                    play = moveOn(preparingTrackPositon);
                    if (!play) {
                        currentTrackPosition = -1;
                        preparingTrackPositon = -1;
                    }
                }
                if (play) {
                    playMusic(user, tracks, preparingTrackPositon + 1);
                }
                notification.showFailure(result);
            }
        });
    }

    public void resumeMusic() {
        requestAudioFocus();
    }

    private void playMusic() {
        synchronized (trackLock) {
            if (currentTrackPosition < 0) {
                return;
            }
            if (listener != null) {
                listener.onPlay(tracks, currentTrackPosition);
            }
            seekTo(lastMusicPosition);
            exoPlayer.play();
            notification.showPlay(tracks.get(currentTrackPosition));
        }
    }

    public void pauseMusic() {
        synchronized (trackLock) {
            if (listener != null && currentTrackPosition >= 0) {
                listener.onPause(tracks, currentTrackPosition);
            }
            lastMusicPosition = getCurrentPosition();
            exoPlayer.pause();
            notification.showPause();
            synchronized (focusLock) {
                resumeOnFocusGain = false;
            }
        }
    }

    public void seekTo(long position) {
        lastMusicPosition = position;
        exoPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return exoPlayer.isPlaying();
    }

    public int getCurrentTrackPosition() {
        return currentTrackPosition;
    }

    public int getPreparingTrackPosition() {
        return preparingTrackPositon;
    }

    public List<YoutubeSearchResult> getTracks() {
        synchronized (trackLock) {
            return Collections.unmodifiableList(tracks);
        }
    }

    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public boolean isPreparing() {
        synchronized (trackLock) {
            return preparingTrackPositon >= 0;
        }
    }

    private void requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int ret;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ret = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            ret = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        synchronized (focusLock) {
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                playbackDelayed = false;
            } else if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                playbackDelayed = false;
                playMusic();
            } else if (ret == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                playbackDelayed = true;
            }
        }
    }

    @Override
    public void onCompletion(ExoPlayerWrapper exoPlayer) {
        pauseMusic();
        boolean play;
        synchronized (trackLock) {
            lastMusicPosition = 0;
            play = moveOn(currentTrackPosition);
        }
        if (play) {
            playMusic(user, tracks, currentTrackPosition + 1);
        }
    }

    private boolean moveOn(int position) {
        return position >= 0 && position + 1 < tracks.size() && user != null;
    }

    @Override
    public void onError(ExoPlayerWrapper exoPlayer, ExoPlaybackException error) {
        synchronized (trackLock) {
            if (preparingTrackPositon >= 0) {
                if (listener != null) {
                    listener.onFailure(Status.ServerOffline, tracks, preparingTrackPositon);
                }
                notification.showFailure(tracks.get(preparingTrackPositon));
                if (moveOn(preparingTrackPositon)) {
                    playMusic(user, tracks, preparingTrackPositon + 1);
                } else {
                    preparingTrackPositon = -1;
                }
            } else if (currentTrackPosition >= 0) {
                if (listener != null) {
                    listener.onFailure(Status.ServerOffline, tracks, currentTrackPosition);
                }
                notification.showFailure(tracks.get(currentTrackPosition));
                if (moveOn(currentTrackPosition)) {
                    playMusic(user, tracks, currentTrackPosition + 1);
                } else {
                    currentTrackPosition = -1;
                }
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        try {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (playbackDelayed || resumeOnFocusGain) {
                        synchronized (focusLock) {
                            playbackDelayed = false;
                            resumeOnFocusGain = false;
                        }
                        exoPlayer.setVolume(1.0f);
                        playMusic();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    synchronized (focusLock) {
                        resumeOnFocusGain = false;
                        playbackDelayed = false;
                    }
                    pauseMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    synchronized (focusLock) {
                        resumeOnFocusGain = isPlaying();
                        playbackDelayed = false;
                    }
                    pauseMusic();
                    break;
            }
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        server = new YoutubeServer(this);

        exoPlayer = new ExoPlayerWrapper(this);
        exoPlayer.setOnCompletionListener(this);
        exoPlayer.setOnErrorListener(this);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }

        notification = new MusicPlayerNotification(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MUSIC_PLAYER_STOP);
        filter.addAction(ACTION_MUSIC_PLAY_PAUSE);
        filter.addAction(ACTION_MUSIC_PREVIOUS);
        filter.addAction(ACTION_MUSIC_NEXT);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (listener != null) {
            listener.onDisconnect();
        }

        server.close();
        exoPlayer.release();
        unregisterReceiver(receiver);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(this);

        stopForeground(false);
    }

    public void setListener(MusicPlayerListener listener) {
        this.listener = listener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public boolean isBound() {
        return isBound;
    }

    public void onBind() {
        isBound = true;
        notification.refresh();
    }

    public void onUnbind() {
        isBound = false;
        notification.refresh();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
