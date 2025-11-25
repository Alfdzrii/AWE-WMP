package com.example.awe;


import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Import yang dibutuhkan dari Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Import library lain yang dibutuhkan
import com.bumptech.glide.Glide;
import com.example.awe.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import de.hdodenhof.circleimageview.CircleImageView; // Import untuk CircleImageView

public class ProfileFragment extends Fragment {

    // Deklarasi variabel untuk Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Deklarasi variabel untuk UI
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private Toolbar toolbar;
    private TextView textUsername;
    private TextView textBio; // Jika Anda ingin mengatur bio juga
    private CircleImageView profileImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inisialisasi Views dari layout
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        toolbar = view.findViewById(R.id.toolbar);
        textUsername = view.findViewById(R.id.text_username);
        textBio = view.findViewById(R.id.text_bio); // Inisialisasi TextView untuk bio
        profileImage = view.findViewById(R.id.profile_image);

        // Panggil method untuk update UI
        updateProfileUI();

        // Mengatur Toolbar
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                // Judul di Toolbar akan muncul saat header di-scroll ke atas
                toolbar.setTitle("Profil");
            }
        }

        // Setup ViewPager2 dan Adapter
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Menghubungkan TabLayout dengan ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Playlist");
                            break;
                        case 1:
                            tab.setText("Favorit");
                            break;
                        case 2:
                            tab.setText("Baru Didengar");
                            break;
                    }
                }
        ).attach();
    }

    /**
     * Method untuk mengambil data dari FirebaseUser dan memperbarui UI
     */
    private void updateProfileUI() {
        if (currentUser != null) {
            // 1. Mengambil dan mengatur Nama Pengguna (DisplayName)
            String username = currentUser.getDisplayName();
            if (username != null && !username.isEmpty()) {
                textUsername.setText(username);
            } else {
                // Jika nama tidak ada (misalnya login via email tanpa set nama),
                // tampilkan placeholder atau bagian dari email.
                textUsername.setText("Pengguna Baru");
            }

            // 2. Mengambil dan mengatur Foto Profil
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null && getContext() != null) {
                // Gunakan library seperti Glide atau Picasso untuk memuat gambar dari URL
                Glide.with(getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile) // Gambar default saat loading
                        .error(R.drawable.ic_profile)       // Gambar jika gagal load
                        .into(profileImage);
            }

            // Contoh: Mengatur email sebagai bio jika bio kosong
            // String email = currentUser.getEmail();
            // textBio.setText(email);
        }
    }
}
