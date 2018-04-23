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
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;

public class MusicPlayerService extends Service
        implements AudioManager.OnAudioFocusChangeListener,
        ExoPlayerWrapper.OnCompletionListener, ExoPlayerWrapper.OnErrorListener {

    private static final String NAME = MusicPlayerService.class.getName();
    public static final String ACTION_MUSIC_PLAYER_STOP = NAME + ".ACTION.MUSIC_PLAYER_STOP";
    public static final String ACTION_MUSIC_PLAY_PAUSE = NAME + ".ACTION.MUSIC_PLAY_PAUSE";

    private boolean isBounded;

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
    private YoutubeSearchResult currentTrack;
    private long lastMusicPosition;

    private YoutubeSearchResult preparingTrack;

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_MUSIC_PLAYER_STOP)) {
                if (!isBounded()) {
                    stopSelf();
                }
            } else if (intent.getAction().equals(ACTION_MUSIC_PLAY_PAUSE)) {
                if (isPlaying()) {
                    pauseMusic();
                } else {
                    requestAudioFocus();
                }
            } else if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pauseMusic();
            }
        }
    };

    public synchronized void playMusic(User user, YoutubeSearchResult result) {
        pauseMusic();
        server.close();

        preparingTrack = result;
        if (listener != null) {
            listener.onFetchingSong(result);
        }

        Youtube youtube = new Youtube();
        youtube.apikey = user.apikey;
        youtube.id = result.id;
        youtube.addhistory = true;

        notification.showProgress(result);
        server.fetchSong(youtube, new YoutubeServer.YoutubeSongIdCallback() {
            @Override
            public void onSuccess(String url) {
                exoPlayer.setDataSource(url);
                exoPlayer.setOnPreparedListener(exoPlayer -> {
                    synchronized (trackLock) {
                        currentTrack = result;
                        lastMusicPosition = 0;
                    }
                    requestAudioFocus();
                });
            }

            @Override
            public void onFailure(int code) {
                if (listener != null) {
                    listener.onFailure(result);
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
            if (currentTrack == null) {
                return;
            }
            if (listener != null) {
                listener.onPlay(currentTrack);
            }
            seekTo(lastMusicPosition);
            exoPlayer.play();
            notification.showPlay(currentTrack);
        }
    }

    public void pauseMusic() {
        synchronized (trackLock) {
            if (listener != null && currentTrack != null) {
                listener.onPause(currentTrack);
            }
            lastMusicPosition = getCurrentPosition();
            exoPlayer.pause();
            notification.showPause();
        }
        synchronized (focusLock) {
            resumeOnFocusGain = false;
        }
    }

    public void seekTo(long position) {
        lastMusicPosition = position;
        exoPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return exoPlayer.isPlaying();
    }

    public YoutubeSearchResult getCurrentTrack() {
        return currentTrack;
    }

    public YoutubeSearchResult getPreparingTrack() {
        return preparingTrack;
    }

    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return exoPlayer.getDuration();
    }

    public boolean isPreparing() {
        return exoPlayer.isPreparing();
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
    }

    @Override
    public void onError(ExoPlayerWrapper exoPlayer, ExoPlaybackException error) {
        synchronized (trackLock) {
            if (preparingTrack != null) {
                if (listener != null) {
                    listener.onFailure(preparingTrack);
                }
                notification.showFailure(preparingTrack);
            } else if (currentTrack != null) {
                if (listener != null) {
                    listener.onFailure(currentTrack);
                }
                notification.showFailure(currentTrack);
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
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        server.close();

        pauseMusic();
        exoPlayer.release();

        unregisterReceiver(receiver);

        stopForeground(false);
    }

    public void setListener(MusicPlayerListener listener) {
        this.listener = listener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public boolean isBounded() {
        return isBounded;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onBind() {
        isBounded = true;
        notification.refresh();
    }

    public void onUnbind() {
        isBounded = false;
        notification.refresh();
    }
}
