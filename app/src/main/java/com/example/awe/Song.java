package com.example.awe;

import android.net.Uri;

public class Song {
    private String title;
    private String artist;
    private Uri audioUri;
    private boolean isFavorite;

    public Song(String title, String artist, Uri audioUri) {
        this.title = title;
        this.artist = artist;
        this.audioUri = audioUri;
        this.isFavorite = false;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getAudioUri() {
        return audioUri;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
