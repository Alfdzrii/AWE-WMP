// Pastikan nama package ini sesuai dengan lokasi file Anda
package com.example.awe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;// Kelas ini adalah "Adapter" yang akan memberitahu ViewPager2
// fragment mana yang harus ditampilkan untuk setiap tab.
public class ProfilePagerAdapter extends FragmentStateAdapter {

    // Konstruktor kelas
    public ProfilePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Metode ini akan membuat dan mengembalikan fragment yang sesuai
        // berdasarkan posisi tab yang dipilih.
        switch (position) {
            case 0:
                // Posisi 0 (Tab "Playlist") akan menampilkan PlaylistsFragment
                return new PlaylistsFragment();
            case 1:
                // Posisi 1 (Tab "Favorit") akan menampilkan FavoritesFragment
                return new FavoritesFragment();
            case 2:
                // Posisi 2 (Tab "Baru Didengar") akan menampilkan HistoryFragment
                return new HistoryFragment();
            default:
                // Sebagai tindakan pengamanan, jika posisi tidak dikenali,
                // tampilkan fragment pertama.
                return new PlaylistsFragment();
        }
    }

    @Override
    public int getItemCount() {
        // Metode ini memberitahu ViewPager2 berapa total jumlah tab/halaman yang ada.
        // Karena Anda punya 3 tab, kita kembalikan nilai 3.
        return 3;
    }
}
