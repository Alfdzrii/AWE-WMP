package com.example.awe;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements MusicAdapter.OnItemInteractionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "HomeFragment";
    private FloatingActionButton fabAddMusic;
    private RecyclerView recyclerViewMusic;
    private MusicAdapter musicAdapter;
    private final ArrayList<MusicItem> displayedMusicList = new ArrayList<>();
    private final ArrayList<MusicItem> fullMusicList = new ArrayList<>();

    private EditText searchBar;

    private MediaPlayer mediaPlayer;
    private MusicItem currentPlayingItem = null;
    private int currentPlayingPosition = -1;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String[]> selectAudioLauncher;

    // Firebase & Firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference musicCollection;
    private ListenerRegistration musicListenerRegistration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            db = FirebaseFirestore.getInstance();
            musicCollection = db.collection("users").document(currentUser.getUid()).collection("music");
        } else {
            Toast.makeText(getContext(), "Harap login untuk mengakses musik.", Toast.LENGTH_LONG).show();
        }

        selectAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null && musicCollection != null) {
                        copyFileToInternalStorage(uri);
                    } else {
                        Toast.makeText(getContext(), "Tidak ada lagu yang dipilih.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        selectAudioLauncher.launch(new String[]{"audio/*"});
                    } else {
                        Toast.makeText(getContext(), "Izin diperlukan untuk menambah musik", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void copyFileToInternalStorage(Uri uri) {
        String originalFileName = getFileName(requireContext(), uri);
        if (originalFileName == null) {
            originalFileName = "music_" + System.currentTimeMillis();
        }

        File internalFile = new File(requireContext().getFilesDir(), originalFileName);

        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(internalFile)) {

            if (inputStream == null) {
                throw new IOException("Gagal membuka input stream untuk URI");
            }

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            MusicItem newItem = new MusicItem(originalFileName, internalFile.getAbsolutePath());
            saveMusicItemToFirestore(newItem);

        } catch (IOException e) {
            Log.e(TAG, "Gagal menyalin file ke internal storage", e);
            Toast.makeText(getContext(), "Gagal memproses file lagu.", Toast.LENGTH_SHORT).show();
        }
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
        searchBar = view.findViewById(R.id.search_bar);

        setupRecyclerView();
        fabAddMusic.setOnClickListener(v -> checkPermissionAndOpenPicker());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMusic(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMusic(String query) {
        displayedMusicList.clear();
        if (query.isEmpty()) {
            displayedMusicList.addAll(fullMusicList);
        } else {
            for (MusicItem item : fullMusicList) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    displayedMusicList.add(item);
                }
            }
        }
        if (musicAdapter != null) {
            musicAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMusicFromFirestore();
    }

    private void loadMusicFromFirestore() {
        if (musicCollection == null) return;
        musicListenerRegistration = musicCollection.orderBy("title").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshots != null) {
                fullMusicList.clear();
                for (QueryDocumentSnapshot doc : snapshots) {
                    MusicItem item = doc.toObject(MusicItem.class);
                    item.setId(doc.getId());
                    fullMusicList.add(item);
                }
                filterMusic(searchBar.getText().toString());
            }
        });
    }

    private void saveMusicItemToFirestore(MusicItem item) {
        musicCollection.add(item)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Lagu berhasil disimpan dengan ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Lagu ditambahkan: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Gagal menyimpan lagu", e);
                    Toast.makeText(getContext(), "Gagal menambahkan lagu.", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkPermissionAndOpenPicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            selectAudioLauncher.launch(new String[]{"audio/*"});
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void setupRecyclerView() {
        musicAdapter = new MusicAdapter(displayedMusicList, this);
        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMusic.setAdapter(musicAdapter);
    }

    @Override
    public void onFavoriteClicked(MusicItem item, int position) {
        if (musicCollection == null || item.getId() == null) {
            Toast.makeText(getContext(), "Gagal, user tidak login atau lagu tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean newFavoriteState = !item.isFavorite();
        musicCollection.document(item.getId()).update("favorite", newFavoriteState);
    }

    @Override
    public void onDeleteClicked(MusicItem item, int position) {
        if (musicCollection == null || item.getId() == null) {
            Toast.makeText(getContext(), "Gagal, user tidak login atau lagu tidak valid.", Toast.LENGTH_SHORT).show();
            return;
        }
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
                    Toast.makeText(getContext(), "Lagu '" + item.getTitle() + "' dihapus", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal menghapus lagu.", Toast.LENGTH_SHORT).show());
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
            String path = clickedItem.getFilePath();

            // 1. Cek Validitas Path
            if (path == null || path.isEmpty()) {
                Toast.makeText(getContext(), "Error: Path kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(path);

            // 2. Cek apakah file ada DAN ukurannya tidak 0
            if (!file.exists()) {
                Toast.makeText(getContext(), "File hilang dari HP.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (file.length() == 0) {
                Log.e(TAG, "File ditemukan tapi ukurannya 0 bytes (Kosong). Copy gagal.");
                Toast.makeText(getContext(), "File lagu rusak (0 bytes). Hapus dan upload ulang.", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. --- PERUBAHAN UTAMA: Gunakan FileInputStream & FileDescriptor ---
            // Ini meminimalisir error permission pada Internal Storage
            java.io.FileInputStream fis = new java.io.FileInputStream(file);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD()); // Menggunakan File Descriptor

            fis.close(); // Tutup stream setelah diserahkan ke MediaPlayer

            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnCompletionListener(mp -> stopCurrentMusic());

            Log.d(TAG, "Sedang menyiapkan lagu via FileDescriptor. Size: " + file.length());
            mediaPlayer.prepareAsync();

            currentPlayingItem = clickedItem;
            currentPlayingPosition = clickedPosition;

        } catch (IOException e) {
            Log.e(TAG, "Gagal memutar (IO Error): ", e);
            Toast.makeText(getContext(), "Error memutar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            stopCurrentMusic();
        } catch (Exception e) {
            Log.e(TAG, "Gagal memutar (General Error): ", e);
            stopCurrentMusic();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
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
        String errorMessage = "Error Code: (" + what + ", " + extra + ")";
        Log.e(TAG, "MediaPlayer Error: " + errorMessage);

        // Tampilkan kode error langsung di layar HP
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

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
            if (recyclerViewMusic != null && musicAdapter != null) {
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
        if (musicListenerRegistration != null) {
            musicListenerRegistration.remove();
        }
    }

    private String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) result = cursor.getString(nameIndex);
                }
            }
        } else {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
