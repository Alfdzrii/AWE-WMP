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
import android.widget.Toast; // <-- TAMBAHKAN INI
import android.widget.EditText; // <-- TAMBAHKAN INI
import android.text.InputType; // <-- TAMBAHKAN INI
import androidx.appcompat.app.AlertDialog; // <-- TAMBAHKAN INI
import android.graphics.Color;

// Import yang dibutuhkan dari Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // <-- TAMBAHKAN INI
import com.google.firebase.auth.UserProfileChangeRequest; // <-- TAMBAHKAN INI

// Import library lain yang dibutuhkan
import com.bumptech.glide.Glide;
import com.example.awe.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // Deklarasi variabel untuk Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    // Deklarasi variabel untuk UI
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private Toolbar toolbar;
    private TextView textUsername;
    private TextView textBio; // Jika Anda ingin mengatur bio juga
    private CircleImageView profileImage;
    private ImageView editProfileImageButton;


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
        db = FirebaseFirestore.getInstance();


        // Inisialisasi Views dari layout
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        toolbar = view.findViewById(R.id.toolbar);
        textUsername = view.findViewById(R.id.text_username);
        textBio = view.findViewById(R.id.text_bio); // Inisialisasi TextView untuk bio
        profileImage = view.findViewById(R.id.profile_image);
        editProfileImageButton = view.findViewById(R.id.edit_profile_image_button);


        // Panggil method untuk update UI
        updateProfileUI();

        editProfileImageButton.setOnClickListener(v -> showEditPhotoDialog());
        textBio.setOnClickListener(v -> showEditBioDialog());

        // Mengatur Toolbar
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                // Judul di Toolbar akan muncul saat header di-scroll ke atas
                toolbar.setTitle("Profile");
                toolbar.setTitleTextColor(Color.WHITE);
            }
        }

        // Setup ViewPager2 dan Adapter
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Menghubungkan TabLayout dengan ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Karena hanya ada satu tab, kita langsung set teksnya
                    tab.setText("Favorites");
                }
        ).attach();
    }

    /**
     * Method untuk mengambil data dari FirebaseUser dan memperbarui UI
     */
    private void updateProfileUI() {
        if (currentUser != null) {
            // 1. Mengambil dan mengatur Nama Pengguna (DisplayName) - (Sudah ada)
            String username = currentUser.getDisplayName();
            if (username != null && !username.isEmpty()) {
                textUsername.setText(username);
            } else {
                textUsername.setText("New User");
            }

            // 2. Mengambil dan mengatur Foto Profil - (Sudah ada)
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null && getContext() != null) {
                Glide.with(getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(profileImage);
            }

            // --- TAMBAHKAN BLOK INI UNTUK MENGAMBIL BIO ---
            // 3. Mengambil dan mengatur Bio dari Firestore
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String bio = documentSnapshot.getString("bio");
                            if (bio != null && !bio.isEmpty()) {
                                textBio.setText(bio);
                            } else {
                                textBio.setText("Tap to edit bio"); // Placeholder jika bio kosong
                            }
                        } else {
                            // Dokumen belum ada, buat dengan bio kosong
                            // Ini penting agar updateBioInFirestore() tidak gagal nanti
                            db.collection("users").document(currentUser.getUid()).set(new java.util.HashMap<>());
                            textBio.setText("Tap to edit bio");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textBio.setText("Bio not available");
                    });
            // ----------------------------------------------------
        }
    }


    /**
     * Menampilkan dialog untuk memasukkan URL gambar baru.
     */
    private void showEditPhotoDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile Photo");
        builder.setMessage("Please enter a new image URL.");

        // Buat EditText untuk dialog
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI); // Tipe input untuk URL
        builder.setView(input);

        // Atur tombol "Save"
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPhotoUrl = input.getText().toString().trim();
            if (!newPhotoUrl.isEmpty()) {
                // Panggil metode untuk memperbarui foto
                updateProfilePhoto(newPhotoUrl);
            } else {
                Toast.makeText(getContext(), "URL cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Atur tombol "Cancel"
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Menampilkan dialog untuk mengedit bio.
     */
    private void showEditBioDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Bio");

        final EditText input = new EditText(getContext());
        input.setText(textBio.getText()); // Tampilkan bio saat ini
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newBio = input.getText().toString().trim();
            updateBioInFirestore(newBio);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Memperbarui URL foto profil di Firebase Auth dan Firestore.
     * @param photoUrl URL gambar baru.
     */
    private void updateProfilePhoto(String photoUrl) {
        if (currentUser == null) return;

        Uri newUri = Uri.parse(photoUrl);

        // 1. Perbarui di Firebase Authentication
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(newUri)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 2. Jika berhasil, perbarui juga di Firestore agar sinkron
                db.collection("users").document(currentUser.getUid())
                        .update("photoUrl", photoUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                            // Muat gambar baru menggunakan Glide
                            if(getContext() != null) {
                                Glide.with(getContext()).load(photoUrl).into(profileImage);
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Failed to update photo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Memperbarui bio di koleksi 'users' pada Firestore.
     * @param newBio Bio baru yang akan disimpan.
     */
    private void updateBioInFirestore(String newBio) {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid())
                .update("bio", newBio)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Bio updated!", Toast.LENGTH_SHORT).show();
                    textBio.setText(newBio); // Perbarui UI secara langsung
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update bio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}