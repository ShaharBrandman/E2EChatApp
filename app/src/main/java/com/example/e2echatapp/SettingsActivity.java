package com.example.e2echatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    
    private Button goBack, signOut, deleteUser, takeProfilePicture, saveChangesBtn;
    private TextView emailTv, userIdTv;
    private ImageView profilePic;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String userId = auth.getCurrentUser().getUid();
    private File profilePicFile = new File(
            Environment.getExternalStorageDirectory() +
                    File.separator + userId +
                    ".jpg");
    private Bitmap bitmapImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        goBack = findViewById(R.id.goBackButton);
        signOut = findViewById(R.id.signOut);
        deleteUser = findViewById(R.id.deleteUser);
        takeProfilePicture = findViewById(R.id.changeProfilePicTv);
        saveChangesBtn = findViewById(R.id.saveChanges);

        emailTv = findViewById(R.id.emailTv);
        userIdTv = findViewById(R.id.userId);

        profilePic = findViewById(R.id.profilePic);

        userIdTv.setText(userId);

        emailTv.setText(auth.getCurrentUser().getEmail());

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

        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
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
            bitmapImage = (Bitmap) data.getExtras().get("data");
            profilePic.setImageBitmap(bitmapImage);
        }
    }

    private void saveChanges() {
        if (bitmapImage != null) {
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 60, imageBytes);

            byte[] data = imageBytes.toByteArray();

            StorageReference storageRef = FirebaseStorage
                    .getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(SettingsActivity.this, "Couldn't update profile picture", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    try {
                        profilePicFile.createNewFile();
                        FileOutputStream output = new FileOutputStream(profilePicFile);
                        output.write(data);
                        output.close();

                        //profilePic.setImageBitmap(BitmapFactory.decodeFile(profilePicFile.getPath()));
                        Toast.makeText(SettingsActivity.this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}