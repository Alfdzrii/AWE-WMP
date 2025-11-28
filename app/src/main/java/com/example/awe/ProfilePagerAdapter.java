// Pastikan nama package ini sesuai dengan lokasi file Anda
package com.example.awe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // TODO: Ganti dengan Fragment Playlist Anda jika sudah ada
                return new PlaceholderFragment(); 
            case 1:
                // Ini adalah tab Favorit, kita gunakan fragment yang sudah kita buat
                return new FavoriteSongsFragment(); 
            case 2:
                // TODO: Ganti dengan Fragment History Anda jika sudah ada
                return new PlaceholderFragment(); 
            default:
                return new PlaceholderFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
