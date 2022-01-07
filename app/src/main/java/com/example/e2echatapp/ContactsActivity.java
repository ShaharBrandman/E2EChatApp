package com.example.e2echatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;

import static com.example.e2echatapp.api.contacts.addContact;
import static com.example.e2echatapp.api.contacts.deleteContact;
import static com.example.e2echatapp.api.contacts.getContacts;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    
    private ListView listview;
    private Button settingsButton, addContactBtn;
    private AlertDialog dialog = null;
    private static ArrayList<Contact> contacts = new ArrayList<Contact>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().hide();

        listview = findViewById(R.id.contactsListView);
        settingsButton = findViewById(R.id.settingsButton);

        addContactBtn = findViewById(R.id.AddContactBtn);

        setContacts();

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

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog == null) {
                    final View addContactView = LayoutInflater.from(ContactsActivity.this).inflate(R.layout.add_contact, null);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);

                    builder.setView(addContactView);
                    builder.setCancelable(true);

                    addContactView.findViewById(R.id.AddContactBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final EditText userID = addContactView.findViewById(R.id.userId);
                            final EditText username = addContactView.findViewById(R.id.username);

                            addContact(ContactsActivity.this, userID.getText().toString(), username.getText().toString());
                            setContacts();

                            dialog.hide();
                        }
                    });

                    dialog = builder.create();

                }

                dialog.show();
            }
        });
    }

    private void setContacts() {
        JSONArray arr = getContacts(getApplicationContext());
        contacts = new ArrayList<>();

        for(int i=0; i<arr.length(); i++) {
            try {
                contacts.add(new Contact(
                        arr.getJSONObject(i).get("nickname").toString(),
                        arr.getJSONObject(i).get("lastMessage").toString(),
                        arr.getJSONObject(i).getLong("lastTimeStamp"),
                        R.drawable.ic_launcher_background
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ContactsAdapter adapter = new ContactsAdapter(this, contacts);

        listview.setAdapter(adapter);
    }

    private class ContactsAdapter extends ArrayAdapter<Contact> {
        public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Contact contact = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_list_view, parent, false);
            }

            TextView contactName = convertView.findViewById(R.id.contactName);
            TextView lastMessage = convertView.findViewById(R.id.lastMessage);
            TextView time = convertView.findViewById(R.id.lastTime);
            ImageView profilePic = convertView.findViewById(R.id.contactPic);
            Button deleteBtn = convertView.findViewById(R.id.deleteBtn);

            contactName.setText(contact.getContact());
            lastMessage.setText(contact.getLastMessage());

            Timestamp tmp = new Timestamp(contact.getTime());
            time.setText(tmp.getHours() + ":" + tmp.getMinutes());

            profilePic.setImageResource(contact.getImage());
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteContact(ContactsActivity.this, contact.getContact());
                    setContacts();
                }
            });

            return convertView;
        }
    }

    private class Contact {
        private String contact;
        private String lastMessage;
        private long time;
        private int image;

        public Contact(String contact, String lastMessage, long time, int image) {
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

        public long getTime() {
            return this.time;
        }

        public int getImage() {
            return this.image;
        }
    }
}