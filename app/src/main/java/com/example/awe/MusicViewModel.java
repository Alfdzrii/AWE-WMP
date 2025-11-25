package com.example.awe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class MusicViewModel extends ViewModel {

    private final MutableLiveData<List<Song>> allSongs = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    public void addSong(Song song) {
        List<Song> currentList = allSongs.getValue();
        if (currentList != null) {
            currentList.add(song);
            allSongs.setValue(currentList); // This will notify observers
        }
    }
}
