package com.example.awe;

import android.net.Uri;
import com.google.firebase.firestore.Exclude;
import java.io.File;

public class MusicItem {
    private String id;
    private String title;
    private String filePath; // Menggantikan uriString untuk kejelasan

    // Field ini TIDAK disimpan di database, hanya untuk UI
    @Exclude
    private boolean isPlaying = false;

    private boolean isFavorite = false;

    // Konstruktor kosong untuk Firestore
    public MusicItem() {}

    public MusicItem(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Membuat Uri dari file path. Metode ini lebih handal.
    @Exclude
    public Uri getUri() {
        if (filePath == null) return null;
        return Uri.fromFile(new File(filePath));
    }

    @Exclude
    public boolean isPlaying() {
        return isPlaying;
    }

    @Exclude
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