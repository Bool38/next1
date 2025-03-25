package com.example.osignup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private CardView cardApple;
    private Button btnLogout;
    private ImageButton btnEditProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        cardApple = findViewById(R.id.cardApple);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Check if user is logged in
        if (currentUser == null) {
            // Redirect to login
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Set email from Firebase Auth
        if (currentUser.getEmail() != null) {
            tvEmail.setText(currentUser.getEmail());
        }

        // Get username from database
        String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).child("profile").child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Bug Fix: Handle username properly
                            String username = dataSnapshot.getValue(String.class);
                            if (username != null && !username.isEmpty()) {
                                tvUsername.setText(username);
                            } else {
                                tvUsername.setText("User");
                                // If username doesn't exist, create one based on email
                                createDefaultUsername(userId, currentUser.getEmail());
                            }
                        } else {
                            tvUsername.setText("User");
                            // If username node doesn't exist, create one based on email
                            createDefaultUsername(userId, currentUser.getEmail());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        tvUsername.setText("User");
                        Toast.makeText(DashboardActivity.this,
                                "Failed to load profile: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Button click listeners
        cardApple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, AppleSectionActivity.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                finish();
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create default username if not found
    private void createDefaultUsername(String userId, String email) {
        if (email != null) {
            String username = email.substring(0, email.indexOf('@'));
            mDatabase.child("users").child(userId).child("profile").child("username").setValue(username);
            tvUsername.setText(username);
        }
    }
}