package com.example.e2echatapp.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashMap;

public class contacts extends fileSystem {
    private static final String TAG = "contacts";
    private static FirebaseDatabase db = FirebaseDatabase.getInstance();
    private static DatabaseReference reference = null;

    public contacts() {
        super();
    }

    public static JSONArray getContacts(Context context) {
        try {
            return new JSONArray(getDataFromFile(context, "Contacts.json"));
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public static void addContact(Context context, String userId, String nickname) {
        String contacts = getDataFromFile(context, "Contacts.json");

        try {
            JSONArray arr = null;

            if (!contacts.isEmpty())  {
                arr = new JSONArray(contacts);
            }
            else {
                arr = new JSONArray();
            }

            JSONObject obj = new JSONObject();

            obj.put("userId", userId);
            obj.put("nickname", nickname);
            obj.put("publicKey", userId);
            obj.put("privateKey", "");
            obj.put("lastTimeStamp", new Timestamp(System.currentTimeMillis()).getTime());
            obj.put("lastMessage", "Press to chat.");

            arr.put(obj);

            writeToFile(context, "Contacts.json", arr.toString());

            FirebaseDatabase.getInstance().getReference(obj.getString("publicKey")).setValue("Just created the text");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void changeContactNickname(Context context, String contact, String newNickname) {
        try {
            JSONArray contacts = new JSONArray(getDataFromFile(context, "Contacts.json"));

            for(int i=0; i<contacts.length(); i++) {
                if (contacts.getJSONObject(i).get("nickname").equals(contact)) {
                    contacts.getJSONObject(i).put("nickname", newNickname);
                }
            }

            writeToFile(context, "Contacts.json", contacts.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void changeLastMessage(Context context, String contact, String newMessage) {
        try {
            JSONArray contacts = new JSONArray(getDataFromFile(context, "Contacts.json"));

            for(int i=0; i<contacts.length(); i++) {
                if (contacts.getJSONObject(i).get("nickname").equals(contact)) {
                    contacts.getJSONObject(i).put("lastMessage", newMessage);
                }
            }

            writeToFile(context, "Contacts.json", contacts.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteContact(Context context, String contact) {
        try {
            JSONArray contacts = new JSONArray(getDataFromFile(context, "Contacts.json"));
            JSONArray tmp = new JSONArray();

            for(int i=0; i<contacts.length(); i++) {
                if (!contacts.getJSONObject(i).get("nickname").equals(contact)) {
                    tmp.put(contacts.getJSONObject(i));
                }
            }

            writeToFile(context, "Contacts.json", tmp.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray fetchContactChat(Context context, String username) {
        try {
            return new JSONArray(getDataFromFile(context, username + ".json"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getPublicKey(Context context, String contact) {
        try {
            JSONArray contacts = new JSONArray(getDataFromFile(context, "Contacts.json"));

            for(int i=0; i<contacts.length(); i++) {
                if (contacts.getJSONObject(i).get("nickname").equals(contact)) {
                    return contacts.getJSONObject(i).getString("publicKey");
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void sendMessage(Context context, String contact, final String message) {
        try {
            JSONArray contacts = new JSONArray(getDataFromFile(context, "Contacts.json"));

            for(int i=0; i<contacts.length(); i++) {
                if (contacts.getJSONObject(i).get("nickname").equals(contact)) {
                    reference = db.getReference(contacts.getJSONObject(i).getString("publicKey"));

                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String, HashMap<String, Object>> messages = new HashMap<>();

                            JSONArray data = null;
                            try {
                                data = new JSONArray(new Gson().toJson(dataSnapshot.getValue(Object.class)));
                                Log.d(TAG, "data length is: " + data.length());
                                for(int i=0; i<data.length(); i++) {
                                    HashMap<String, Object> existingMsg = new HashMap<>();
                                    existingMsg.put("sender", data.getJSONObject(i).getString("sender"));
                                    existingMsg.put("message", data.getJSONObject(i).getString("message"));
                                    existingMsg.put("timestamp", data.getJSONObject(i).getString("timestamp"));
                                    messages.put(String.valueOf(i), existingMsg);
                                    //Log.d(TAG, "index: " + data.get(i).toString());
                                }
                            } catch (JSONException e) {
                                data = new JSONArray();
                                e.printStackTrace();
                            }
                            //Log.d(TAG, "messages: " + messages.toString());
                            HashMap<String, Object> newMsg = new HashMap<>();
                            newMsg.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            newMsg.put("timestamp", new Timestamp(System.currentTimeMillis()).getTime());
                            newMsg.put("message", message);

                            if (data != null) {
                                //Log.d(TAG, "data lenght is " + data.length());
                                messages.put(String.valueOf(data.length()), newMsg);
                            }
                            else {
                                //Log.d(TAG, "data lenght is 0, data is null");
                                messages.put(String.valueOf(0), newMsg);
                            }

                            reference.setValue(messages);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "failed, " + databaseError.getMessage());
                        }
                    });
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
