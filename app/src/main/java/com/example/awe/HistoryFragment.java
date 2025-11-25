// Pastikan package sesuai dengan proyek Anda
package com.example.awe;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

// Anda perlu membuat Adapter dan Model untuk History
// import com.example.awe.adapters.HistoryAdapter;
// import com.example.awe.models.Song;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    // private HistoryAdapter adapter; // Adapter untuk RecyclerView
    // private ArrayList<Song> songHistoryList; // List untuk menampung data lagu

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Meng-inflate layout XML untuk fragment ini
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi RecyclerView dari layout
        recyclerView = view.findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inisialisasi list dan adapter
        // songHistoryList = new ArrayList<>();
        // adapter = new HistoryAdapter(getContext(), songHistoryList);
        // recyclerView.setAdapter(adapter);

        // Panggil method untuk memuat data riwayat dari Firebase atau sumber lain
        loadSongHistory();
    }

    private void loadSongHistory() {
        // --- TAHAP SELANJUTNYA ---
        // Di sinilah Anda akan menulis kode untuk mengambil data dari Firebase Firestore
        // atau Realtime Database.

        // --- UNTUK SEKARANG, GUNAKAN DATA DUMMY (PALSU) UNTUK PENGUJIAN ---
        // Hapus bagian ini jika Anda sudah siap mengimplementasikan Firebase.
        // songHistoryList.add(new Song("Bohemian Rhapsody", "Queen", "url_album_1"));
        // songHistoryList.add(new Song("Smells Like Teen Spirit", "Nirvana", "url_album_2"));
        // songHistoryList.add(new Song("Hotel California", "Eagles", "url_album_3"));
        // adapter.notifyDataSetChanged(); // Memberitahu adapter bahwa ada data baru
    }
}
