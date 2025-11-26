package com.example.awe;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

// Implementasikan listener untuk MediaPlayer
public class HomeFragment extends Fragment implements MusicAdapter.OnItemInteractionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "HomeFragment";
    private static final String PREFS_NAME = "FavoriteSongsPrefs";
    private FloatingActionButton fabAddMusic;
    private RecyclerView recyclerViewMusic;
    private MusicAdapter musicAdapter;
    private final ArrayList<MusicItem> musicList = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    private MediaPlayer mediaPlayer;
    private MusicItem currentPlayingItem = null;
    private int currentPlayingPosition = -1;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> selectAudioLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        selectAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String songTitle = getFileName(requireContext(), uri);
                        MusicItem newItem = new MusicItem(songTitle, uri);
                        newItem.setFavorite(isFavorite(newItem.getTitle()));
                        musicList.add(newItem);
                        musicAdapter.notifyItemInserted(musicList.size() - 1);
                        Toast.makeText(getContext(), "Lagu ditambahkan: " + songTitle, Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        selectAudioLauncher.launch("audio/*");
                    } else {
                        Toast.makeText(getContext(), "Izin diperlukan untuk menambah musik", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fabAddMusic = view.findViewById(R.id.fab_add_music);
        recyclerViewMusic = view.findViewById(R.id.recycler_view_music);
        setupRecyclerView();
        fabAddMusic.setOnClickListener(v -> checkPermissionAndOpenPicker());
    }

    private void checkPermissionAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectAudioLauncher.launch("audio/*");
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void setupRecyclerView() {
        musicAdapter = new MusicAdapter(musicList, this);
        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMusic.setAdapter(musicAdapter);
    }

    @Override
    public void onFavoriteClicked(MusicItem item, int position) {
        item.setFavorite(!item.isFavorite());
        saveFavoriteState(item.getTitle(), item.isFavorite());
        musicAdapter.notifyItemChanged(position);
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
            mediaPlayer.setDataSource(requireContext(), clickedItem.getUri());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync(); // Gunakan prepareAsync

            // Tandai item yang diklik sebagai item yang sedang diputar
            currentPlayingItem = clickedItem;
            currentPlayingPosition = clickedPosition;

        } catch (IOException e) {
            Log.e(TAG, "setDataSource failed", e);
            Toast.makeText(getContext(), "Gagal memutar file", Toast.LENGTH_SHORT).show();
            stopCurrentMusic(); // Bersihkan jika gagal
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Dipanggil ketika media player siap
        Log.d(TAG, "MediaPlayer is prepared. Starting playback.");
        mp.start();

        if (currentPlayingItem != null) {
            currentPlayingItem.setPlaying(true);
            musicAdapter.notifyItemChanged(currentPlayingPosition);
        }
        mp.setOnCompletionListener(mediaPlayer -> stopCurrentMusic());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Dipanggil jika ada kesalahan
        Log.e(TAG, "MediaPlayer Error: what=" + what + ", extra=" + extra);
        Toast.makeText(getContext(), "Tidak dapat memutar lagu ini.", Toast.LENGTH_SHORT).show();
        stopCurrentMusic();
        return true; // Menandakan bahwa kesalahan telah ditangani
    }

    private void stopCurrentMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (currentPlayingItem != null) {
            currentPlayingItem.setPlaying(false);
            musicAdapter.notifyItemChanged(currentPlayingPosition);
            currentPlayingItem = null;
            currentPlayingPosition = -1;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCurrentMusic();
    }

    private void saveFavoriteState(String songTitle, boolean isFavorite) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(songTitle, isFavorite);
        editor.apply();
    }

    private boolean isFavorite(String songTitle) {
        return sharedPreferences.getBoolean(songTitle, false);
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        } else {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }
}
