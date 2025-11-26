package com.example.awe;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class SplashActivity extends AppCompatActivity {

    // Durasi splash screen dalam milidetik
    private static final int SPLASH_DURATION = 2000; // 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView splashTitle = findViewById(R.id.splash_title);

        // 1. Terapkan font kustom
        try {
            // Menggunakan nama font yang benar: breath_of_the_river
            Typeface customFont = ResourcesCompat.getFont(this, R.font.breath_of_the_river);
            splashTitle.setTypeface(customFont);
        } catch (Exception e) {
            // Jika font tidak ditemukan, biarkan menggunakan font default
            e.printStackTrace();
        }

        // 2. Muat dan jalankan animasi
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        splashTitle.startAnimation(slideUp);

        // 3. Pindah ke LoginActivity setelah durasi tertentu
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Tutup SplashActivity agar tidak bisa kembali
        }, SPLASH_DURATION);
    }
}