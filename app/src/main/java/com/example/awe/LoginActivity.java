package com.example.awe;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    // 1. Deklarasikan variabel untuk view (komponen)
    EditText editTextEmail, editTextPassword;
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Hubungkan variabel dengan ID di file XML
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // 3. Beri aksi pada tombol (setOnClickListener)
        buttonLogin.setOnClickListener(v -> {
            // Ambil teks dari EditText
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            // Validasi sederhana (nanti bisa diganti database)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            } else {
                // Anggap login berhasil jika diisi
                Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();

                // Pindah ke MainActivity (tempat teman Anda)
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // Tutup LoginActivity agar tidak bisa kembali
                finish();
            }
        });
    }
}