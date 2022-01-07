package com.example.e2echatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;

import static com.example.e2echatapp.api.contacts.*;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "LogInActivity";
    
    private static EditText email, emailPassword;
    private static Button signIn, signUp;

    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setTitle("Login to your account");

        email = findViewById(R.id.email);
        emailPassword = findViewById(R.id.emailPassword);

        signIn = findViewById(R.id.continueSignIn);
        signUp = findViewById(R.id.continueSignUp);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signInWithEmailAndPassword(email.getText().toString(), emailPassword.getText().toString())
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail: success");
                                    startActivity(new Intent(LogInActivity.this, ContactsActivity.class));
                                } else {
                                    Log.w(TAG, "signInWithEmail: failure", task.getException());
                                    Toast.makeText(LogInActivity.this, "Email or password is wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.createUserWithEmailAndPassword(email.getText().toString(), emailPassword.getText().toString())
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createdUsernWithEmail: success");
                                    startActivity(new Intent(LogInActivity.this, ContactsActivity.class));
                                } else {
                                    Log.w(TAG, "createdUserWithEmail: failure", task.getException());
                                    Toast.makeText(LogInActivity.this, "Email or password is wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}