package com.example.osignup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignup, btnLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogin = findViewById(R.id.btnLogin);

        // Set up Sign Up button click listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set up Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login Activity
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser() {
        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Show progress
        ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully
                            String userId = mAuth.getCurrentUser().getUid();

                            // Reference to users node in Realtime Database
                            DatabaseReference userRef = mDatabase.child("users").child(userId);

                            // Create user data map with more information
                            HashMap<String, Object> userData = new HashMap<>();
                            userData.put("userId", userId);
                            userData.put("email", email);
                            userData.put("name", email.substring(0, email.indexOf('@'))); // Default name from email
                            userData.put("createdAt", ServerValue.TIMESTAMP);
                            userData.put("accountType", "standard");

                            // Write user data to database
                            userRef.setValue(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            // Dismiss progress dialog
                                            progressDialog.dismiss();

                                            if (dbTask.isSuccessful()) {
                                                // Database write successful
                                                Toast.makeText(SignupActivity.this,
                                                        "Account created successfully",
                                                        Toast.LENGTH_SHORT).show();

                                                // Navigate to main activity
                                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Database write failed
                                                Toast.makeText(SignupActivity.this,
                                                        "Failed to store user data: " + dbTask.getException().getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            // Dismiss progress dialog
                            progressDialog.dismiss();

                            // Authentication failed
                            Toast.makeText(SignupActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}