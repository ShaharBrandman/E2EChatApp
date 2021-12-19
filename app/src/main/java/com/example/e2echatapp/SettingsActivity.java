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
    private TextView emailTv;
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

        emailTv.setText(auth.getCurrentUser().getEmail());

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ContactsActivity.class));
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().delete();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
            }
        });
    }
}