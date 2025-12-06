
package com.example.awe;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements ProfileAdapter.OnProfileInteractionListener {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonLoginOtherAccount;
    private TextView textViewRegister, textViewAppTitle;
    private Group groupProfileList, groupLoginForm;
    private RecyclerView recyclerViewProfiles;
    private AccountManager accountManager;
    private ProfileAdapter profileAdapter;
    private List<SavedAccount> savedAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        accountManager = new AccountManager(this);

        findViewsById();
        applyCustomFont();
        setupClickListeners();
        
        // Pengecekan dipindah ke onStart()
    }

    @Override
    protected void onStart() {
        super.onStart();
        // PERIKSA APAKAH PENGGUNA SUDAH LOGIN DARI SESI SEBELUMNYA
        if (mAuth.getCurrentUser() != null) {
            // Jika ya, langsung masuk (inilah fitur "cookie" atau "ingat saya")
            navigateToHome();
        } else {
            // Jika tidak, tampilkan halaman login (daftar profil atau form)
            updateLoginUI();
        }
    }

    private void findViewsById() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        recyclerViewProfiles = findViewById(R.id.recycler_view_profiles);
        buttonLoginOtherAccount = findViewById(R.id.button_login_other_account);
        textViewAppTitle = findViewById(R.id.text_view_app_title);
        groupProfileList = findViewById(R.id.group_profile_list);
        groupLoginForm = findViewById(R.id.group_login_form);
    }

    private void applyCustomFont() {
        try {
            Typeface customFont = ResourcesCompat.getFont(this, R.font.breath_of_the_river);
            textViewAppTitle.setTypeface(customFont);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> handleLoginButtonClick());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, MainActivity.class)));
        buttonLoginOtherAccount.setOnClickListener(v -> showLoginForm());
    }

    private void updateLoginUI() {
        savedAccounts = accountManager.getSavedAccounts();
        if (savedAccounts.isEmpty()) {
            showLoginForm();
        } else {
            showProfileList();
        }
    }

    private void showProfileList() {
        groupProfileList.setVisibility(View.VISIBLE);
        groupLoginForm.setVisibility(View.GONE);
        profileAdapter = new ProfileAdapter(savedAccounts, this);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProfiles.setAdapter(profileAdapter);
    }

    private void showLoginForm() {
        groupProfileList.setVisibility(View.GONE);
        groupLoginForm.setVisibility(View.VISIBLE);
    }

    private void handleLoginButtonClick() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email can't be empty");
            editTextEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Password can't be empty");
            editTextPassword.requestFocus();
            return;
        }
        loginUserWithFirebase(email, password);
    }

    private void loginUserWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserAndProceed(user);
                        }
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserAndProceed(FirebaseUser user) {
        SavedAccount account = new SavedAccount(user.getUid(), user.getEmail());
        accountManager.saveOrUpdateAccount(account);
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onProfileClicked(SavedAccount account) {
        mAuth.signOut();
        showLoginForm();
        editTextEmail.setText(account.getEmail());
        editTextPassword.requestFocus();
        Toast.makeText(this, "Please enter password for " + account.getEmail(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveClicked(SavedAccount account, int position) {
        accountManager.removeAccount(account.getUid());
        savedAccounts.remove(position);
        profileAdapter.notifyItemRemoved(position);

        if (savedAccounts.isEmpty()) {
            showLoginForm();
        }
    }
}
