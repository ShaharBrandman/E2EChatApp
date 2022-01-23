package com.example.e2echatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.e2echatapp.api.contacts.addMessageToChat;
import static com.example.e2echatapp.api.contacts.changeContactNickname;
import static com.example.e2echatapp.api.contacts.sendMessage;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private Button goBackButton;
    private EditText contactName, keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        goBackButton = findViewById(R.id.goBackButton);

        contactName = findViewById(R.id.contactName);
        keyboard = findViewById(R.id.keyboard);

        contactName.setText(getIntent().getStringExtra("contact"));

        contactName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                changeContactNickname(ChatActivity.this, getIntent().getStringExtra("contact"), contactName.getText().toString());
                return false;
            }
        });

        keyboard.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                sendMessage(ChatActivity.this, getIntent().getStringExtra("contact"), keyboard.getText().toString());
                return false;
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, ContactsActivity.class));
            }
        });
    }
}