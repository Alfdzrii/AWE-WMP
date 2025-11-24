package com.example.awe;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

//REGISTER PAGE
public class MainActivity extends AppCompatActivity {
    Button BtnRegist;
    TextView textViewLogin;
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
        textViewLogin = findViewById(R.id.textViewLogin);
        BtnRegist = findViewById(R.id.BtnRegist);
        Auth = FirebaseAuth.getInstance();

        BtnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = TxtEmail.getText().toString();
                String Password = TxtPassword.getText().toString();
                String Username = TxtUsername.getText().toString();

                if ( TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password) || TextUtils.isEmpty(Username) ){
                    Toast.makeText(getApplicationContext(), "Input username and password", Toast.LENGTH_SHORT).show();
                } else {
                    registUser(Email, Password, Username);
                }
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            }
        });
    }

    private void registUser(String email, String password, final String username) {
        Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = Auth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Success to Register!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));

                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Register is denied: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
