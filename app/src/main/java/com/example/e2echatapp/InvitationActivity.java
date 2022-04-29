package com.example.e2echatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;

public class InvitationActivity extends AppCompatActivity {
    private static final String TAG = "InvitationActivity";

    private ListView listView;
    private Button backButton;
    private EditText searchContacts;
    private ArrayList<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }
        else {
            showActivityContent();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showActivityContent();
            }
            else {
                Toast.makeText(this, "Cannot show contacts until you will provide permissions", Toast.LENGTH_LONG).show();
                startActivity(new Intent(InvitationActivity.this, ContactsActivity.class));
                finish();
            }
        }
    }

    //show content after has accepted permissions and contacts list is not empty
    private void showActivityContent() {
        ContentResolver resolver = getContentResolver();

        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        );

        if (cursor != null) {
            HashSet<String> mobileSet = new HashSet<String>();

            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);

                    number = cursor.getString(numberIndex);
                    number = number.replace(" ", "");

                    if (!mobileSet.contains(number)) {
                        mobileSet.add(number);
                        contacts.add(new Contact(name, number));
                    }
                }
            }
            finally {
                if (contacts.size() == 0) {
                    Toast.makeText(InvitationActivity.this, "Your contacts list is empty!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(InvitationActivity.this, ContactsActivity.class));
                    finish();
                }
                listView = findViewById(R.id.contactsListView);
                backButton = findViewById(R.id.backButton);
                searchContacts = findViewById(R.id.searchContacts);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(InvitationActivity.this, ContactsActivity.class));
                        finish();
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(
                                contacts.get(i).phoneNumber,
                                null,
                                "Hi! I'm inviting you to chat with me on E2EChatApp, My UserID is: " + FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                null,
                                null
                        );
                        Toast.makeText(InvitationActivity.this,  contacts.get(i).nickname + " has been invited!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(InvitationActivity.this, ContactsActivity.class));
                        finish();
                    }
                });

                searchContacts.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        showContacts(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                listView.setAdapter(new ContactsListView(InvitationActivity.this, contacts));
                cursor.close();
            }
        }
    }

    private void showContacts(String match) {
        ArrayList<Contact> tmp = new ArrayList<>();

        for(int i=0; i<contacts.size(); i++) {
            if (contacts.get(i).nickname.contains(match)) {
                tmp.add(contacts.get(i));
            }
        }

        listView.setAdapter(new ContactsListView(InvitationActivity.this, tmp));
    }

    private class ContactsListView extends ArrayAdapter<Contact> {
        public ContactsListView(@NonNull Context context, ArrayList<Contact> contacts) {
            super(context, 0, contacts);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Contact contact = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.invite_contacts_list_view, parent, false);
            }

            TextView nickname = convertView.findViewById(R.id.contactNickname);
            TextView phoneNumber = convertView.findViewById(R.id.contactPhoneNumber);

            nickname.setText(contact.nickname);
            phoneNumber.setText(contact.phoneNumber);

            return convertView;
        }
    }

    private class Contact {
        public String nickname;
        public String phoneNumber;

        public Contact(String nickname, String phoneNumber) {
            this.nickname = nickname;
            this.phoneNumber = phoneNumber;
        }
    }
}