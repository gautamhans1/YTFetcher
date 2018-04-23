package com.grarak.ytfetcher.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.grarak.ytfetcher.LoginActivity;
import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicPlayerNotification {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL = "music_channel";

    private Executor executor = Executors.newSingleThreadExecutor();

    private NotificationManager manager;
    private MusicPlayerService service;

    private AtomicBoolean fetching = new AtomicBoolean();
    private AtomicBoolean playing = new AtomicBoolean();
    private YoutubeSearchResult result;
    private Bitmap playingBitmap;

    MusicPlayerNotification(MusicPlayerService service) {
        this.service = service;

        manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    private Bitmap getBitmap(String url) {
        try {
            return Glide.with(service).asBitmap().load(url).submit().get();
        } catch (Exception ignored) {
            return BitmapFactory.decodeResource(service.getResources(), R.mipmap.ic_launcher);
        }
    }

    private PendingIntent getBoardcast(String action) {
        return PendingIntent.getBroadcast(service, 0, new Intent(action), 0);
    }

    void showProgress(YoutubeSearchResult result) {
        fetching.set(true);
        playing.set(false);
        this.result = result;
        executor.execute(() -> {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
                            .setContentTitle(service.getString(R.string.loading))
                            .setContentText(result.title)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setProgress(0, 0, true);
            service.startForeground(NOTIFICATION_ID, builder.build());
        });
    }

    void showFailure(YoutubeSearchResult result) {
        fetching.set(false);
        playing.set(false);
        this.result = result;
        executor.execute(() -> {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
                            .setContentTitle(service.getString(R.string.failed))
                            .setContentText(result.title)
                            .setSmallIcon(R.mipmap.ic_launcher);
            service.startForeground(NOTIFICATION_ID, builder.build());
        });
    }

    void showPlay(YoutubeSearchResult result) {
        fetching.set(false);
        playing.set(true);
        this.result = result;
        executor.execute(() -> {
            NotificationCompat.Builder builder = baseBuilder(result,
                    playingBitmap = getBitmap(result.thumbnail));
            builder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_pause,
                    service.getString(R.string.play),
                    getBoardcast(MusicPlayerService.ACTION_MUSIC_PLAY_PAUSE)));

            service.startForeground(NOTIFICATION_ID, builder.build());
        });
    }

    void showPause() {
        fetching.set(false);
        playing.set(false);
        if (result == null) return;
        executor.execute(() -> {
            NotificationCompat.Builder builder = baseBuilder(result,
                    playingBitmap == null ? getBitmap(result.thumbnail) : playingBitmap);
            builder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_play,
                    service.getString(R.string.play),
                    getBoardcast(MusicPlayerService.ACTION_MUSIC_PLAY_PAUSE)));

            service.startForeground(NOTIFICATION_ID, builder.build());
        });
    }

    void refresh() {
        if (playing.get()) {
            showPlay(result);
        } else if (fetching.get()) {
            showProgress(result);
        } else {
            showPause();
        }
    }

    private NotificationCompat.Builder baseBuilder(
            YoutubeSearchResult result, Bitmap bitmap) {
        Intent intent = new Intent(service, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(service, 0, intent, 0);

        String title = result.title;
        String contentText = result.id;
        if (title.length() > 20) {
            String tmp = title.substring(20);
            int whitespaceIndex = tmp.indexOf(' ');
            if (whitespaceIndex >= 0) {
                int firstWhitespace = 20 + tmp.indexOf(' ');
                contentText = title.substring(firstWhitespace + 1);
                title = title.substring(0, firstWhitespace);
            }
        }

        boolean isBound = service.isBounded();

        android.support.v4.media.app.NotificationCompat.MediaStyle mediaStyle =
                new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle();
        mediaStyle.setShowActionsInCompactView(isBound ? 0 : 1);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL)
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setSubText(result.duration)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(bitmap)
                        .setContentIntent(contentIntent)
                        .setDeleteIntent(getBoardcast(MusicPlayerService.ACTION_MUSIC_PLAYER_STOP))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setStyle(mediaStyle);
        if (!isBound) {
            builder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_stop,
                    service.getString(R.string.stop),
                    getBoardcast(MusicPlayerService.ACTION_MUSIC_PLAYER_STOP)));
        }
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (manager.getNotificationChannel(NOTIFICATION_CHANNEL) != null) {
            return;
        }
        manager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL, service.getString(R.string.music_player),
                NotificationManager.IMPORTANCE_LOW));
    }
}
