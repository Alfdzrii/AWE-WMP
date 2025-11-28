package com.example.awe;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FavoriteSongsFragment extends Fragment implements MusicAdapter.OnItemInteractionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "FavoriteSongsFragment";
    private RecyclerView recyclerViewFavorites;
    private MusicAdapter musicAdapter;
    private final ArrayList<MusicItem> favoriteList = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private MusicItem currentPlayingItem = null;
    private int currentPlayingPosition = -1;

    private FirebaseFirestore db;
    private CollectionReference musicCollection;
    private ListenerRegistration favoritesListenerRegistration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            db = FirebaseFirestore.getInstance();
            musicCollection = db.collection("users").document(currentUser.getUid()).collection("music");
        } else {
            Toast.makeText(getContext(), "Harap login untuk melihat favorit.", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewFavorites = view.findViewById(R.id.recycler_view_favorites);
        setupRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavoriteSongs();
    }

    private void loadFavoriteSongs() {
        if (musicCollection == null) return;

        favoritesListenerRegistration = musicCollection.whereEqualTo("favorite", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        favoriteList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            MusicItem item = doc.toObject(MusicItem.class);
                            item.setId(doc.getId());
                            favoriteList.add(item);
                        }
                        musicAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupRecyclerView() {
        musicAdapter = new MusicAdapter(favoriteList, this);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFavorites.setAdapter(musicAdapter);
    }

    @Override
    public void onFavoriteClicked(MusicItem item, int position) {
        if (musicCollection == null || item.getId() == null) return;

        // Menghapus dari daftar favorit dengan mengubah status favorite menjadi false
        musicCollection.document(item.getId()).update("favorite", false)
                .addOnSuccessListener(aVoid -> {
                    // Lagu akan otomatis hilang dari daftar karena listener Firestore aktif
                    Toast.makeText(getContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPlayPauseClicked(MusicItem clickedItem, int clickedPosition) {
        if (currentPlayingItem == clickedItem) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                clickedItem.setPlaying(false);
            } else if (mediaPlayer != null) {
                mediaPlayer.start();
                clickedItem.setPlaying(true);
            }
            musicAdapter.notifyItemChanged(clickedPosition);
            return;
        }

        stopCurrentMusic();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setDataSource(clickedItem.getFilePath());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();

            currentPlayingItem = clickedItem;
            currentPlayingPosition = clickedPosition;

        } catch (IOException e) {
            Log.e(TAG, "setDataSource failed", e);
            stopCurrentMusic();
        }
    }

    @Override
    public void onDeleteClicked(MusicItem item, int position) {
        if (musicCollection == null || item.getId() == null) return;

        if (item == currentPlayingItem) {
            stopCurrentMusic();
        }

        musicCollection.document(item.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    File fileToDelete = new File(item.getFilePath());
                    if (fileToDelete.exists() && fileToDelete.delete()) {
                        Log.d(TAG, "File internal berhasil dihapus: " + item.getFilePath());
                    } else {
                        Log.w(TAG, "Gagal menghapus file internal: " + item.getFilePath());
                    }
                    Toast.makeText(getContext(), "Lagu dihapus permanen", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (currentPlayingItem != null) {
            currentPlayingItem.setPlaying(true);
            musicAdapter.notifyItemChanged(currentPlayingPosition);
        }
        mp.setOnCompletionListener(mediaPlayer -> stopCurrentMusic());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer Error: what=" + what + ", extra=" + extra);
        Toast.makeText(getContext(), "Tidak dapat memutar lagu ini.", Toast.LENGTH_SHORT).show();
        stopCurrentMusic();
        return true;
    }

    private void stopCurrentMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (currentPlayingItem != null) {
            currentPlayingItem.setPlaying(false);
            if (recyclerViewFavorites != null && musicAdapter != null) {
                musicAdapter.notifyItemChanged(currentPlayingPosition);
            }
            currentPlayingItem = null;
            currentPlayingPosition = -1;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCurrentMusic();
        if (favoritesListenerRegistration != null) {
            favoritesListenerRegistration.remove();
        }
    }
}
