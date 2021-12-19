package com.example.e2echatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    
    private ListView listview;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().hide();

        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        contacts.add(new Contact("ShaharBrandman", "PP", "16:00", R.drawable.ic_launcher_background));

        ContactsAdapter adapter = new ContactsAdapter(this, contacts);

        listview = findViewById(R.id.contactsListView);
        settingsButton = findViewById(R.id.settingsButton);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                intent.putExtra("contact", contacts.get(i).getContact());
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactsActivity.this, SettingsActivity.class));
            }
        });
    }

    private class ContactsAdapter extends ArrayAdapter<Contact> {
        public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Contact contact = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_list_view, parent, false);
            }

            TextView contactName = convertView.findViewById(R.id.contactName);
            TextView lastMessage = convertView.findViewById(R.id.lastMessage);
            TextView time = convertView.findViewById(R.id.lastTime);
            ImageView profilePic = convertView.findViewById(R.id.contactPic);

            contactName.setText(contact.getContact());
            lastMessage.setText(contact.getLastMessage());
            time.setText(contact.getTime());
            profilePic.setImageResource(contact.getImage());

            return convertView;
        }
    }

    private class Contact {
        private String contact;
        private String lastMessage;
        private String time;
        private int image;

        public Contact(String contact, String lastMessage, String time, int image) {
            this.contact = contact;
            this.lastMessage = lastMessage;
            this.time = time;
            this.image = image;
        }

        public String getContact() {
            return this.contact;
        }

        public String getLastMessage() {
            return this.lastMessage;
        }

        public String getTime() {
            return this.time;
        }

        public int getImage() {
            return this.image;
        }
    }
}