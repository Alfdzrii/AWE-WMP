package com.example.awe;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private Toolbar toolbar;
    private TextView textUsername;
    private TextView textBio;
    private CircleImageView profileImage;
    private ImageView editProfileImageButton;
    private TextView textLogout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        toolbar = view.findViewById(R.id.toolbar);
        textUsername = view.findViewById(R.id.text_username);
        textBio = view.findViewById(R.id.text_bio);
        profileImage = view.findViewById(R.id.profile_image);
        editProfileImageButton = view.findViewById(R.id.edit_profile_image_button);
        textLogout = view.findViewById(R.id.text_logout);

        updateProfileUI();

        editProfileImageButton.setOnClickListener(v -> showEditPhotoDialog());
        textBio.setOnClickListener(v -> showEditBioDialog());
        textLogout.setOnClickListener(v -> logoutUser());

        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                toolbar.setTitle("Profile");
                toolbar.setTitleTextColor(Color.WHITE);
            }
        }

        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    tab.setText("Favorites");
                }
        ).attach();
    }

    private void updateProfileUI() {
        if (currentUser != null) {
            String username = currentUser.getDisplayName();
            if (username != null && !username.isEmpty()) {
                textUsername.setText(username);
            } else {
                textUsername.setText("New User");
            }

            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null && getContext() != null) {
                Glide.with(getContext()).load(photoUrl).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).into(profileImage);
            }

            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String bio = documentSnapshot.getString("bio");
                            if (bio != null && !bio.isEmpty()) {
                                textBio.setText(bio);
                            } else {
                                textBio.setText("Tap to edit bio");
                            }
                        } else {
                            db.collection("users").document(currentUser.getUid()).set(new HashMap<>());
                            textBio.setText("Tap to edit bio");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textBio.setText("Bio not available");
                    });
        }
    }

    private void logoutUser() {
        if (getContext() == null) return;
        mAuth.signOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showEditPhotoDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile Photo");
        builder.setMessage("Please enter a new image URL.");
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPhotoUrl = input.getText().toString().trim();
            if (!newPhotoUrl.isEmpty()) {
                updateProfilePhoto(newPhotoUrl);
            } else {
                Toast.makeText(getContext(), "URL cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditBioDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Bio");
        final EditText input = new EditText(getContext());
        input.setText(textBio.getText());
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newBio = input.getText().toString().trim();
            updateBioInFirestore(newBio);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateProfilePhoto(String photoUrl) {
        if (currentUser == null) return;
        Uri newUri = Uri.parse(photoUrl);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(newUri)
                .build();
        currentUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                db.collection("users").document(currentUser.getUid())
                        .update("photoUrl", photoUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                            if (getContext() != null) {
                                Glide.with(getContext()).load(photoUrl).into(profileImage);
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Failed to update photo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBioInFirestore(String newBio) {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid())
                .update("bio", newBio)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Bio updated!", Toast.LENGTH_SHORT).show();
                    textBio.setText(newBio);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update bio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
