package com.example.awe;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    Button BtnRegist, BtnLogin;
    EditText TxtEmail, TxtPassword, TxtUsername;
    FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TxtEmail = findViewById(R.id.TxtEmail);
        TxtPassword = findViewById(R.id.TxtPassword);
        TxtUsername = findViewById(R.id.TxtUsername);
        BtnLogin = findViewById(R.id.BtnLogin);
        BtnRegist = findViewById(R.id.BtnRegist);
        Auth = FirebaseAuth.getInstance();

        BtnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = TxtEmail.getText().toString();
                String Password = TxtPassword.getText().toString();
                String Username = TxtUsername.getText().toString();

                if ( TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(Username) ){

                }
            }
        });
    }
}