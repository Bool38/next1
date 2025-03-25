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

import java.util.HashMap;
import java.util.Map;

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
        btnSignup.setOnClickListener(v -> registerUser());

        // Set up Login button click listener
        btnLogin.setOnClickListener(v -> {
            // Navigate to Login Activity
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
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
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss(); // Ensure progress dialog is dismissed

                    if (task.isSuccessful()) {
                        // User created successfully
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference userRef = mDatabase.child("users").child(userId);

                        // Create user profile data
                        Map<String, Object> profileData = new HashMap<>();
                        profileData.put("username", email.substring(0, email.indexOf('@'))); // Default username from email
                        profileData.put("email", email);

                        // Create structured database format
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("profile", profileData);
                        userData.put("sections", new HashMap<>()); // Placeholder for future sections

                        // Store structured data in Firebase
                        userRef.setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(SignupActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    } else {
                        // Authentication failed
                        Toast.makeText(SignupActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
