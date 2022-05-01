package com.example.e2echatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    
    private Button goBack, signOut, deleteUser, takeProfilePicture;
    private TextView emailTv, userId;
    private ImageView profilePic;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        goBack = findViewById(R.id.goBackButton);
        signOut = findViewById(R.id.signOut);
        deleteUser = findViewById(R.id.deleteUser);
        takeProfilePicture = findViewById(R.id.changeProfilePicTv);

        emailTv = findViewById(R.id.emailTv);
        userId = findViewById(R.id.userId);

        profilePic = findViewById(R.id.profilePic);

        userId.setText(auth.getCurrentUser().getUid());

        emailTv.setText(auth.getCurrentUser().getEmail());

        File profilePicFile = new File(
                Environment.getExternalStorageDirectory() +
                        File.separator + auth.getCurrentUser().getUid() +
                        ".jpg");

        if(profilePicFile.exists()) {
            profilePic.setImageBitmap(BitmapFactory.decodeFile(profilePicFile.getPath()));
        }

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ContactsActivity.class));
                finish();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
                finish();
            }
        });

        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.getCurrentUser().delete();
                startActivity(new Intent(SettingsActivity.this, LogInActivity.class));
                finish();
            }
        });

        takeProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                }

                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 102);
                }
                else {
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");

            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 60, imageBytes);

            try {
                File imageFile = new File(
                        Environment.getExternalStorageDirectory() +
                                 File.separator +
                                 auth.getCurrentUser().getUid() +
                                 ".jpg"
                );

                imageFile.createNewFile();

                FileOutputStream output = new FileOutputStream(imageFile);
                output.write(imageBytes.toByteArray());
                output.close();

                profilePic.setImageBitmap(BitmapFactory.decodeFile(imageFile.getPath()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}