package com.example.e2echatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LogInActivity extends AppCompatActivity {
    
    private EditText email, emailPassword;
    private Button signIn, signUp;

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setTitle("Login to your account");

        email = findViewById(R.id.email);
        emailPassword = findViewById(R.id.password);

        signIn = findViewById(R.id.signIn);
        signUp = findViewById(R.id.signUp);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString();
                String emailPasswordString  = emailPassword.getText().toString();

                if (!emailString.isEmpty() && !emailPasswordString.isEmpty()) {
                    auth.signInWithEmailAndPassword(emailString, emailPasswordString)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(LogInActivity.this, ContactsActivity.class));
                                        finish();
                                    }
                                    else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(LogInActivity.this, "Password is invalid", Toast.LENGTH_SHORT).show();
                                        } catch(FirebaseAuthInvalidUserException e) {
                                            Toast.makeText(LogInActivity.this, "User has not been found, Email might be wrong", Toast.LENGTH_SHORT).show();
                                        } catch (FirebaseNetworkException e) {
                                            Toast.makeText(LogInActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(LogInActivity.this, "All fields has to be filled before signing in!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getText().toString();
                String emailPasswordString  = emailPassword.getText().toString();

                if (!emailString.isEmpty() && !emailPasswordString.isEmpty()) {
                    auth.createUserWithEmailAndPassword(email.getText().toString(), emailPassword.getText().toString())
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(LogInActivity.this, ContactsActivity.class));
                                        finish();
                                    } else {
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(LogInActivity.this, "Password is invalid", Toast.LENGTH_SHORT).show();
                                        } catch(FirebaseAuthInvalidUserException e) {
                                            Toast.makeText(LogInActivity.this, "User has not been found, Email might be wrong", Toast.LENGTH_SHORT).show();
                                        } catch (FirebaseNetworkException e) {
                                            Toast.makeText(LogInActivity.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(LogInActivity.this, "All fields has to be filled before signing up!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}