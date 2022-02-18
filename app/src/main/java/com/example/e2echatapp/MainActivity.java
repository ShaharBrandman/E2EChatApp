package com.example.e2echatapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if(auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LogInActivity.class));
        }
        else {
            startActivity(new Intent(this, ContactsActivity.class));
        }
    }
}