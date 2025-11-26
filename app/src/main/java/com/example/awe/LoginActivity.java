package com.example.awe;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    // 1. Deklarasikan variabel
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 3. Hubungkan variabel dengan ID di file XML
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // 4. Aksi saat tombol login diklik
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validasi input
            if (email.isEmpty()) {
                editTextEmail.setError("Email cant be empty");
                editTextEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                editTextPassword.setError("Password cant be empty");
                editTextPassword.requestFocus();
                return;
            }

            // Jika valid, panggil fungsi login ke Firebase
            loginUserWithFirebase(email, password);
        });

        // 5. Aksi saat teks "Daftar di sini" diklik
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        });
    } // <- INI ADALAH AKHIR DARI METODE onCreate()

    private void loginUserWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Jika login berhasil
                        Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();

                        // Pindah ke HomeActivity
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        // Jika login gagal, tampilkan pesan error yang jelas
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                        Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

} // <- INI ADALAH AKHIR DARI KELAS LoginActivity
