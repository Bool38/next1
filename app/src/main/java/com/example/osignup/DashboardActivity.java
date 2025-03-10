package com.example.osignup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;
    private CardView appleCard, plumCard;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        appleCard = findViewById(R.id.appleCard);
        plumCard = findViewById(R.id.plumCard);

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Set email from current user
        tvUserEmail.setText(currentUser.getEmail());

        // Load user data from database
        loadUserData(currentUser.getUid());

        // Set click listeners for category cards
        appleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Spray Scheduling screen
                startActivity(new Intent(DashboardActivity.this, SpraySchedulingActivity.class));
            }
        });

        plumCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, just show a toast message
                Toast.makeText(DashboardActivity.this, "Plum module coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData(String userId) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get user email from database
                    String email = dataSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        // Extract username from email (part before @)
                        String username = email.split("@")[0];
                        // Capitalize first letter
                        username = username.substring(0, 1).toUpperCase() + username.substring(1);
                        tvUserName.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DashboardActivity.this,
                        "Failed to load user data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}