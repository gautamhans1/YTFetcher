package com.grarak.ytfetcher.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.grarak.ytfetcher.service.MusicPlayerListener;
import com.grarak.ytfetcher.service.MusicPlayerService;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

public class MusicManager {

    private Context context;
    private MusicPlayerListener listener;

    private User user;
    private MusicPlayerService service;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (MusicManager.this) {
                MusicPlayerService.MusicPlayerBinder binder =
                        (MusicPlayerService.MusicPlayerBinder) service;
                MusicManager.this.service = binder.getService();
                MusicManager.this.service.setListener(listener);
                MusicManager.this.service.onBind();
                listener.onConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (MusicManager.this) {
                MusicManager.this.service = null;
            }
        }
    };

    public MusicManager(Context context, User user, MusicPlayerListener listener) {
        this.context = context;
        this.user = user;
        this.listener = listener;
    }

    public void onResume() {
        Intent intent = new Intent(context, MusicPlayerService.class);
        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onPause() {
        if (service != null) {
            service.onUnbind();
            service.setListener(null);
        }
        context.unbindService(serviceConnection);
    }

    public void play(YoutubeSearchResult result) {
        synchronized (this) {
            if (service != null) {
                service.playMusic(user, result);
            }
        }
    }

    public void resume() {
        synchronized (this) {
            if (service != null) {
                service.resumeMusic();
            }
        }
    }

    public void pause() {
        synchronized (this) {
            if (service != null) {
                service.pauseMusic();
            }
        }
    }

    public void seekTo(int position) {
        synchronized (this) {
            if (service != null) {
                service.seekTo(position);
            }
        }
    }

    public boolean isPlaying() {
        synchronized (this) {
            if (service != null) {
                return service.isPlaying();
            }
        }
        return false;
    }

    public YoutubeSearchResult getCurrentTrack() {
        synchronized (this) {
            if (service != null) {
                return service.getCurrentTrack();
            }
        }
        return null;
    }

    public YoutubeSearchResult getPreparingTrack() {
        synchronized (this) {
            if (service != null) {
                return service.getPreparingTrack();
            }
        }
        return null;
    }

    public long getCurrentPosition() {
        synchronized (this) {
            if (service != null) {
                return service.getCurrentPosition();
            }
        }
        return 0;
    }

    public long getDuration() {
        synchronized (this) {
            if (service != null) {
                return service.getDuration();
            }
        }
        return 0;
    }

    public boolean isPreparing() {
        synchronized (this) {
            if (service != null) {
                return service.isPreparing();
            }
        }
        return false;
    }
}
