package com.grarak.ytfetcher.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.grarak.ytfetcher.R;
import com.grarak.ytfetcher.utils.Settings;
import com.grarak.ytfetcher.utils.server.user.User;
import com.grarak.ytfetcher.utils.server.youtube.Youtube;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeSearchResult;
import com.grarak.ytfetcher.utils.server.youtube.YoutubeServer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadService extends Service implements DownloadListener {

    private static final String ACTION_CANCEL = DownloadService.class.getName() + ".ACTION.CANCEL";
    private static final String ACTION_CANCEL_ALL = DownloadService.class.getName() + ".ACTION.CANCEL_ALL";
    public static final String ACTION_DOWNLOADED = DownloadService.class.getName() + ".ACTION.DOWNLOADED";
    private static final String INTENT_USER = DownloadService.class.getName() + ".INTENT.USER";
    public static final String INTENT_DOWNLOAD = DownloadService.class.getName() + ".INTENT.DOWNLOAD";

    private static final int NOTIFICATION_ID = 2;
    private static final String NOTIFICATION_CHANNEL = "downloading_channel";

    private User user;
    private YoutubeServer server;

    private final Queue<YoutubeSearchResult> downloadQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean downloading = new AtomicBoolean();
    private Handler handler;
    private DownloadTask downloadTask;

    private boolean isForeground;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        server = new YoutubeServer(this);
        handler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL) == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL,
                        getString(R.string.downloading), NotificationManager.IMPORTANCE_LOW));
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CANCEL);
        intentFilter.addAction(ACTION_CANCEL_ALL);
        registerReceiver(cancelReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        user = null;
        if (downloadTask != null) {
            downloadTask.cancel();
        }
        unregisterReceiver(cancelReceiver);

        stopForeground(true);
        isForeground = false;
    }

    private BroadcastReceiver cancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CANCEL_ALL)) {
                downloadQueue.clear();
            }
            if (downloadTask != null) {
                downloadTask.cancel();
            }
        }
    };

    private void showLoadingNotification(YoutubeSearchResult result) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = getBaseNotification(result)
                .setProgress(0, 0, true);

        if (isForeground) {
            manager.notify(NOTIFICATION_ID, builder.build());
        } else {
            startForeground(NOTIFICATION_ID, builder.build());
            isForeground = true;
        }
    }

    private void showProgressNotification(YoutubeSearchResult result, int progress) {
        if (progress % 20 != 0) return;

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(
                this, 0, new Intent(ACTION_CANCEL), 0);
        PendingIntent pendingIntentCancelAll = PendingIntent.getBroadcast(
                this, 0, new Intent(ACTION_CANCEL_ALL), 0);

        NotificationCompat.Builder builder = getBaseNotification(result)
                .addAction(0, getString(R.string.cancel), pendingIntentCancel)
                .addAction(0, getString(R.string.cancel_all), pendingIntentCancelAll)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private NotificationCompat.Builder getBaseNotification(YoutubeSearchResult result) {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle(result.title)
                .setContentText(getString(R.string.downloading))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_download);
    }

    private void startDownloading() {
        if (downloading.get()) return;
        downloading.set(true);
        YoutubeSearchResult result = downloadQueue.poll();

        fetchUrl(result, 0);
        showLoadingNotification(result);
    }

    private void fetchUrl(YoutubeSearchResult result, int retries) {
        if (retries > 10 || user == null) {
            onFinish(result);
        }

        Youtube youtube = new Youtube();
        youtube.apikey = user.apikey;
        youtube.id = result.id;

        server.fetchSong(youtube, new YoutubeServer.YoutubeSongIdCallback() {
            @Override
            public void onSuccess(String url) {
                Uri uri = Uri.parse(url);
                Uri serverUri = Uri.parse(Settings.getServerUrl(getApplicationContext()));
                if (uri.getHost().equals(serverUri.getHost())
                        && !uri.getQueryParameterNames().contains("url")) {

                    downloadTask = new DownloadTask(DownloadService.this, url, result);
                    downloadTask.execute();
                } else {
                    handler.postDelayed(() -> fetchUrl(result, retries + 1), 1000 * 10);
                }
            }

            @Override
            public void onFailure(int code) {
                onFinish(result);
            }
        });
    }

    @Override
    public void onFinish(YoutubeSearchResult result) {
        if (result.getDownloadPath(this).exists()) {
            Intent intent = new Intent(ACTION_DOWNLOADED);
            intent.putExtra(INTENT_DOWNLOAD, result);
            sendBroadcast(intent);
            result.save(this);
        }

        downloading.set(false);
        if (downloadQueue.size() > 0) {
            startDownloading();
        } else {
            stopSelf();
        }
    }

    @Override
    public void onProgress(YoutubeSearchResult result, int progress) {
        showProgressNotification(result, progress);
    }

    private static class DownloadTask extends AsyncTask<Void, Integer, Void> {

        private final WeakReference<DownloadListener> downloadListenerRef;
        private final String url;
        private final File file;
        private final File downloadingFile;
        private final YoutubeSearchResult result;
        private boolean cancel;

        private HttpURLConnection connection;

        private DownloadTask(DownloadService service,
                             String url, YoutubeSearchResult result) {
            downloadListenerRef = new WeakReference<>(service);
            this.url = url;
            this.file = result.getDownloadPath(service);
            downloadingFile = new File(file.toString() + ".downloading");
            downloadingFile.getParentFile().mkdirs();
            this.result = result;
        }

        private long getContentLength(HttpURLConnection connection) {
            String value;
            if ((value = connection.getHeaderField("Content-Range")) != null) {
                return Long.parseLong(value.substring(value.lastIndexOf('/') + 1));
            } else if ((value = connection.getHeaderField("Content-Length")) != null) {
                return Long.parseLong(value);
            }
            return 0;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (file.exists()) return null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                long total = 0;
                if (downloadingFile.exists()) {
                    long size = downloadingFile.length();
                    connection.setRequestProperty("Range", "bytes=" + size + "-");
                    total += size;
                }
                connection.connect();

                int statusCode = connection.getResponseCode();
                if (statusCode < 200 || statusCode >= 300) {
                    return null;
                }

                long contentLength = getContentLength(connection);
                onProgressUpdate((int) (total / contentLength * 100));

                FileOutputStream outputStream = new FileOutputStream(downloadingFile,
                        statusCode == HttpURLConnection.HTTP_PARTIAL);
                byte[] buf = new byte[8192];

                InputStream inputStream = new DataInputStream(connection.getInputStream());
                int read;

                if (cancel) return null;

                while ((read = inputStream.read(buf)) > 0 && !cancel) {
                    outputStream.write(buf, 0, read);
                    outputStream.flush();
                    total += read;
                    onProgressUpdate((int) (total * 100 / contentLength));
                }

                if (total == contentLength && !cancel) {
                    downloadingFile.renameTo(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DownloadListener downloadListener = downloadListenerRef.get();
            if (downloadListener != null) {
                downloadListener.onProgress(result, values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            DownloadListener downloadListener = downloadListenerRef.get();
            if (downloadListener != null) {
                downloadListener.onFinish(result);
            }
        }

        private void cancel() {
            cancel = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        user = (User) intent.getSerializableExtra(INTENT_USER);
        YoutubeSearchResult result = (YoutubeSearchResult)
                intent.getSerializableExtra(INTENT_DOWNLOAD);

        if (!downloadQueue.contains(result)) {
            downloadQueue.offer(result);
            if (downloadQueue.size() == 1) {
                startDownloading();
            }
        }

        return START_STICKY;
    }

    public static void queueDownload(Context context, User user, YoutubeSearchResult result) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(INTENT_USER, user);
        intent.putExtra(INTENT_DOWNLOAD, result);
        context.startService(intent);
    }
}
