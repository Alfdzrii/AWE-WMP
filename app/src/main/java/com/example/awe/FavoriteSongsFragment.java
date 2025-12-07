package com.example.awe;

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
import java.util.ArrayList;

public class FavoriteSongsFragment extends Fragment implements MusicAdapter.OnItemInteractionListener {

    private static final String TAG = "FavoriteSongsFragment";
    private RecyclerView recyclerViewFavorites;
    private MusicAdapter musicAdapter;
    private final ArrayList<MusicItem> favoriteList = new ArrayList<>();

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

    @Override
    public void onResume() {
        super.onResume();
        // SINKRONISASI ICON SAAT KEMBALI KE LAYAR INI
        // Agar jika lagu sedang diputar di background, icon di sini menyesuaikan (jadi pause)
        updatePlayingStatus();
    }

    private void updatePlayingStatus() {
        if (musicAdapter == null) return;

        // Ambil status play/pause dan path lagu yang sedang main dari Service
        boolean isServicePlaying = MusicPlayer.getInstance().isPlaying();
        String currentPath = MusicPlayer.getInstance().getCurrentPath();

        for (MusicItem item : favoriteList) {
            // Jika path lagu ini sama dengan yang sedang diputar di Service
            if (currentPath != null && currentPath.equals(item.getFilePath())) {
                item.setPlaying(isServicePlaying);
            } else {
                item.setPlaying(false);
            }
        }
        // Beritahu adapter untuk refresh tampilan icon
        musicAdapter.notifyDataSetChanged();
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
                        // Setelah data baru dimuat, sinkronkan status play/pause
                        updatePlayingStatus();
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

        // Un-favorite: Ubah status di Firestore, nanti akan hilang otomatis dari list karena listener
        musicCollection.document(item.getId()).update("favorite", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Dihapus dari favorit", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPlayPauseClicked(MusicItem clickedItem, int clickedPosition) {
        // PERBAIKAN UTAMA:
        // Gunakan MusicPlayer (Global/Service), bukan MediaPlayer lokal.
        // Ini memastikan musik tetap jalan saat pindah layar dan tidak double.
        MusicPlayer.getInstance().playOrPause(clickedItem.getTitle(), clickedItem.getFilePath());

        // Langsung update UI agar responsif
        updatePlayingStatus();
    }

    @Override
    public void onDeleteClicked(MusicItem item, int position) {
        if (musicCollection == null || item.getId() == null) return;

        // Jika lagu yang mau dihapus sedang diputar, stop dulu lewat Service
        String currentPath = MusicPlayer.getInstance().getCurrentPath();
        if (currentPath != null && currentPath.equals(item.getFilePath())) {
            MusicPlayer.getInstance().pause(); // Atau stop() jika ingin benar-benar berhenti
        }

        musicCollection.document(item.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    File fileToDelete = new File(item.getFilePath());
                    if (fileToDelete.exists() && fileToDelete.delete()) {
                        Log.d(TAG, "File internal berhasil dihapus: " + item.getFilePath());
                    }
                    Toast.makeText(getContext(), "Lagu dihapus permanen", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        // Hentikan listener Firestore agar hemat data
        if (favoritesListenerRegistration != null) {
            favoritesListenerRegistration.remove();
        }
        // CATATAN PENTING:
        // Jangan panggil stopMusic() di sini agar musik tetap jalan di background!
    }
}