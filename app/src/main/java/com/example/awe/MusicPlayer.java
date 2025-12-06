package com.example.awe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MusicPlayer {

    private static MusicPlayer instance;
    private MusicService musicService;
    private boolean serviceBound = false;

    private MusicPlayer() { }

    public static synchronized MusicPlayer getInstance() {
        if (instance == null) {
            instance = new MusicPlayer();
        }
        return instance;
    }

    // Panggil ini di onCreate MainActivity atau HomeActivity
    public void bindToService(Context context) {
        if (!serviceBound) {
            Intent playIntent = new Intent(context, MusicService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(playIntent); // Agar service tetap jalan meski activity unbind
        }
    }

    // Panggil ini di onDestroy MainActivity
    public void unbindService(Context context) {
        if (serviceBound) {
            context.unbindService(musicConnection);
            serviceBound = false;
        }
    }

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void playOrPause(String title, String path) {
        if (serviceBound && musicService != null) {
            if (musicService.isPlaying() && path.equals(musicService.getCurrentFilePath())) {
                musicService.pauseMusic();
            } else {
                musicService.playSong(title, path);
            }
        }
    }

    public void pause() {
        if (serviceBound && musicService != null) musicService.pauseMusic();
    }

    public boolean isPlaying() {
        return serviceBound && musicService != null && musicService.isPlaying();
    }

    public String getCurrentPath() {
        return (serviceBound && musicService != null) ? musicService.getCurrentFilePath() : "";
    }

    public MusicService getService() {
        return musicService;
    }
}