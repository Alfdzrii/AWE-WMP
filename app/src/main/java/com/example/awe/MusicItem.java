package com.example.awe;

import android.net.Uri;

public class MusicItem {
    private final String title;
    private final Uri uri;
    private boolean isPlaying = false;
    private boolean isFavorite = false;

    public MusicItem(String title, Uri uri) {
        this.title = title;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
