package com.example.e2echatapp;

import static com.example.e2echatapp.api.contacts.addContact;
import static com.example.e2echatapp.api.contacts.deleteContact;
import static com.example.e2echatapp.api.contacts.getContacts;
import static com.example.e2echatapp.api.contacts.getUserId;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private static final String TAG = "ContactsActivity";
    
    private ListView listview;
    private ImageButton settingsButton, addContactBtn;
    private EditText searchBar;
    private AlertDialog dialog = null;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();

    private DatabaseReference conversationsWithUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().hide();

        searchBar = findViewById(R.id.searchBar);

        listview = findViewById(R.id.contactsListView);

        settingsButton = findViewById(R.id.settingsButton);
        addContactBtn = findViewById(R.id.AddContactBtn);

        setContacts("");

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
                            setContacts("");

                            dialog.cancel();
                        }
                    });

                    addContactView.findViewById(R.id.inviteBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.SEND_SMS},123);
                            }
                            else {
                                startActivity(new Intent(ContactsActivity.this, InvitationActivity.class));
                                finish();
                            }
                        }
                    });

                    dialog = builder.create();

                }

                dialog.show();
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setContacts(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Receiver <- Sender conversation direction
        conversationsWithUser = FirebaseDatabase.getInstance()
                .getReference("unreadMessagesFromUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        conversationsWithUser.addChildEventListener(firebaseListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(ContactsActivity.this, InvitationActivity.class));
                finish();
            }
            else {
                Toast.makeText(ContactsActivity.this, "Can't invite friend until you provide SMS permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversationsWithUser.removeEventListener(firebaseListener);
        finish();
    }

    private void setContacts(String match) {
        JSONArray contactsList = getContacts(getApplicationContext());
        contacts = new ArrayList<>();

        FirebaseDatabase
                .getInstance()
                .getReference("unreadMessagesFromUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            for(int i=0; i<contactsList.length(); i++) {
                                if (contactsList.getJSONObject(i).getString("nickname").contains(match)) {

                                    boolean newMessages = false;
                                    String lastMessage = contactsList.getJSONObject(i).get("lastMessage").toString();
                                    long lastTimeStamp = contactsList.getJSONObject(i).getLong("lastTimeStamp");

                                    for(DataSnapshot child : snapshot.getChildren()) {
                                        if (child.getKey().equals(contactsList.getJSONObject(i).getString("publicKey"))) {
                                            newMessages = true;
                                            JSONArray tmp = new JSONArray(new Gson().toJson(child.getValue(Object.class)));
                                            lastMessage = tmp.getJSONObject(0).getString("message");
                                            lastTimeStamp = tmp.getJSONObject(0).getLong("timestamp");
                                        }
                                    }

                                    contacts.add(new Contact(
                                            contactsList.getJSONObject(i).get("nickname").toString(),
                                            lastMessage,
                                            lastTimeStamp,
                                            contactsList.getJSONObject(i).getString("publicKey"),
                                            newMessages
                                    ));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ContactsAdapter adapter = new ContactsAdapter(ContactsActivity.this, contacts);

                        listview.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private ChildEventListener firebaseListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            setContacts(searchBar.getText().toString());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

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

            if (contact.hasUnreadMessages()) {
                convertView.setBackgroundColor(Color.BLUE);
            }

            TextView contactName = convertView.findViewById(R.id.contactName);
            TextView lastMessage = convertView.findViewById(R.id.lastMessage);
            TextView time = convertView.findViewById(R.id.lastTime);
            ImageView profilePic = convertView.findViewById(R.id.contactPic);
            ImageButton deleteBtn = convertView.findViewById(R.id.deleteBtn);

            contactName.setText(contact.getContact());
            lastMessage.setText(contact.getLastMessage());

            Timestamp tmp = new Timestamp(contact.getTime());
            time.setText(tmp.getHours() + ":" + tmp.getMinutes());

            StorageReference storageRef = FirebaseStorage
                    .getInstance().getReference(contact.getContactId() + ".jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profilePic);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exception.printStackTrace();
                    profilePic.setImageResource(R.drawable.ic_launcher_background);
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteContact(ContactsActivity.this, contact.getContact());
                    setContacts("");
                }
            });

            return convertView;
        }
    }

    private class Contact {
        private String contact;
        private String lastMessage;
        private long time;
        private String contactId;
        private boolean hasUnreadMessages;

        public Contact(String contact, String lastMessage, long time, String contactId, Boolean hasUnreadMessages) {
            this.contact = contact;
            this.lastMessage = lastMessage;
            this.time = time;
            this.contactId = contactId;
            this.hasUnreadMessages = hasUnreadMessages;
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

        public String getContactId() {
            return this.contactId;
        }

        public boolean hasUnreadMessages() {
            return this.hasUnreadMessages;
        }
    }
}