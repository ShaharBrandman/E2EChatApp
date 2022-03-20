package com.example.e2echatapp;

import static com.example.e2echatapp.api.contacts.changeLastMessage;
import static com.example.e2echatapp.api.contacts.fetchContactChat;
import static com.example.e2echatapp.api.contacts.getUserId;
import static com.example.e2echatapp.api.contacts.sendMessage;
import static com.example.e2echatapp.api.contacts.updateChatOnDevice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private Button goBackButton;
    private ImageButton sendButton;
    private EditText contactName, keyboard;
    private ListView actualChat;

    private DatabaseReference conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();
        goBackButton = findViewById(R.id.goBackButton);
        sendButton = findViewById(R.id.sendBtn);

        contactName = findViewById(R.id.contactName);
        keyboard = findViewById(R.id.keyboard);
        actualChat = findViewById(R.id.chat);

        contactName.setText(getIntent().getStringExtra("contact"));

        /*contactName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                changeContactNickname(ChatActivity.this, getIntent().getStringExtra("contact"), contactName.getText().toString());
                return false;
            }
        });*/

        //set the chat from device data once
        setChatOnce();

        //send message button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(ChatActivity.this, getIntent().getStringExtra("contact"), keyboard.getText().toString());
                changeLastMessage(ChatActivity.this, getIntent().getStringExtra("contact"), keyboard.getText().toString());
            }
        });

        //Create go back button
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, ContactsActivity.class));
                finish();
            }
        });

        conversation = FirebaseDatabase.getInstance().getReference("unreadMessagesFromUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getUserId(ChatActivity.this, getIntent().getStringExtra("contact")));

        conversation.addValueEventListener(firebaseListener);
    }

    @Override
    protected void onDestroy() {
        conversation.removeEventListener(firebaseListener);
        super.onDestroy();
        finish();
    }

    private ValueEventListener firebaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.getChildrenCount() != 0) {
                updateChatOnDevice(
                        ChatActivity.this,
                        getUserId(ChatActivity.this, getIntent().getStringExtra("contact")),
                        snapshot
                );
                setChatOnce();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void setChatOnce() {
        ArrayList<JSONObject> chat = new ArrayList<>();

        try {
            //fetch messages from device
            JSONArray messagesFromDevice = fetchContactChat(
                    ChatActivity.this,
                    getUserId(ChatActivity.this, getIntent().getStringExtra("contact"))
            );

            //if there are no messages, create new empty JSON array
            if(messagesFromDevice == null) {
                messagesFromDevice = new JSONArray();
            }

            //add messages to the ArrayList
            for(int i=0; i<messagesFromDevice.length(); i++) {
                chat.add(messagesFromDevice.getJSONObject(i));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //no need to explain
        actualChat.setAdapter(new ChatListView(ChatActivity.this, chat, FirebaseAuth.getInstance().getCurrentUser().getUid()));
    }

    private class ChatListView extends ArrayAdapter<JSONObject> {
        private String userId;

        public ChatListView(Context context, ArrayList<JSONObject> messages, String id) {
            super(context, 0, messages);
            this.userId = id;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final JSONObject message = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_view, parent, false);
            }

            try {
                TextView textMessage = convertView.findViewById(R.id.textMessage);
                TextView time = convertView.findViewById(R.id.time);

                textMessage.setText(message.getString("message"));

                Timestamp tmp = new Timestamp(message.getLong("timestamp"));
                time.setText(tmp.getHours() + ":" + tmp.getMinutes());

                if (message.getString("sender").equals(userId)) {
                    //findViewById(R.id.chatLayout).setPadding(0, 0, 290, 0);
                    TextView sender = convertView.findViewById(R.id.sender);
                    sender.setText("This message is yours:");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }
}