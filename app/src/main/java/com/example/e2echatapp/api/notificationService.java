package com.example.e2echatapp.api;

import static com.example.e2echatapp.api.contacts.getContactNickname;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.e2echatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class notificationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("no yet implemented");
    }

    @Override
    public void onCreate() {

        //Receiver <- Sender conversation direction
        DatabaseReference conversation = FirebaseDatabase.getInstance()
                .getReference("unreadMessagesFromUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //add to the users conversation a child event listener
        conversation.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    //convert the available childs in the firebase conversation to JSONArray
                    JSONArray data = new JSONArray(new Gson().toJson(snapshot.getValue(Object.class)));

                    //for the available children in the conversation
                    //if the ID is in the users' contact list, deploy a notification
                    for(int i=0; i<data.length(); i++) {
                        String contactNickname = getContactNickname(notificationService.this, data.getJSONObject(i).getString("sender"));
                        if (contactNickname != null) {
                            deployNotification(contactNickname);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    private void deployNotification(String contact) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contact)
                .setContentText("Sent you a message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationChannel channel = new NotificationChannel("1", "MAIN", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);
        manager.notify(1, builder.build());


    }
}
