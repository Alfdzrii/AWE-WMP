package com.example.awe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "MusicService";
    private static final String CHANNEL_ID = "MusicChannel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;
    private final IBinder musicBind = new MusicBinder();
    private String currentTitle = "";
    private String currentFilePath = "";
    private boolean isPaused = false;

    // Interface untuk komunikasi balik ke Activity/Fragment (opsional, buat update UI)
    public interface OnMusicStateChangeListener {
        void onStateChanged(boolean isPlaying);
    }
    private OnMusicStateChangeListener stateChangeListener;

    public void setStateChangeListener(OnMusicStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Menangani aksi dari notifikasi (Play/Pause/Close) bisa ditambahkan di sini nanti
        return START_STICKY;
    }

    public void playSong(String title, String path) {
        if (mediaPlayer == null) return;

        // Jika lagu sama dan sedang pause, resume saja
        if (path.equals(currentFilePath) && isPaused) {
            resumeMusic();
            return;
        }

        mediaPlayer.reset();
        currentTitle = title;
        currentFilePath = path;
        isPaused = false;

        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            mediaPlayer.setDataSource(fis.getFD());
            fis.close();
            mediaPlayer.prepareAsync(); // Async agar UI tidak macet
        } catch (Exception e) {
            Log.e(TAG, "Error setting data source", e);
            Toast.makeText(this, "Gagal memutar lagu", Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            showNotification(false); // Update notifikasi jadi icon play
            if (stateChangeListener != null) stateChangeListener.onStateChanged(false);
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
            showNotification(true); // Update notifikasi jadi icon pause
            if (stateChangeListener != null) stateChangeListener.onStateChanged(true);
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPaused = false;
            stopForeground(true); // Hapus notifikasi
            if (stateChangeListener != null) stateChangeListener.onStateChanged(false);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        showNotification(true); // Tampilkan notifikasi saat lagu mulai
        if (stateChangeListener != null) stateChangeListener.onStateChanged(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopForeground(true); // Hapus notifikasi saat lagu selesai
        isPaused = false;
        if (stateChangeListener != null) stateChangeListener.onStateChanged(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Controls for music playback");
            serviceChannel.setSound(null, null); // Penting agar notifikasi tidak bunyi "ting" setiap update
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void showNotification(boolean isPlaying) {
        // Intent untuk membuka aplikasi saat notifikasi diklik
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Icon Play/Pause berdasarkan status
        int icon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
        String playPauseTitle = isPlaying ? "Pause" : "Play";

        // Membangun Notifikasi
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Playing Music")
                .setContentText(currentTitle)
                .setSmallIcon(R.drawable.ic_music_notes) // Pastikan icon ini ada atau ganti ic_launcher
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true) // Agar tidak bunyi terus saat update
                .setOngoing(isPlaying) // Jika playing, notifikasi tidak bisa di-swipe
                .addAction(icon, playPauseTitle, null) // Tombol aksi (belum ada fungsi klik di sini untuk penyederhanaan)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}