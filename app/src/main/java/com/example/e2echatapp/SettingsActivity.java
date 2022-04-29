package com.example.e2echatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private Button goBack, signOut, deleteUser;
    private TextView emailTv, userId;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        goBack = findViewById(R.id.goBackButton);
        signOut = findViewById(R.id.signOut);
        deleteUser = findViewById(R.id.deleteUser);

        emailTv = findViewById(R.id.emailTv);
        userId = findViewById(R.id.userId);

        userId.setText(auth.getCurrentUser().getUid());

        emailTv.setText(auth.getCurrentUser().getEmail());

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ContactsActivity.class));
                finish();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
                finish();
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().delete();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}