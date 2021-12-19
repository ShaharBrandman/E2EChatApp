package com.example.e2echatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private Button goBackButton;
    private EditText contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        goBackButton = findViewById(R.id.goBackButton);

        contactName = findViewById(R.id.contactName);

        contactName.setText(getIntent().getStringExtra("contact"));

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, ContactsActivity.class));
            }
        });
    }
}