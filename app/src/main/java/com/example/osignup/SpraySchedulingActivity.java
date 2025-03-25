package com.example.osignup;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SpraySchedulingActivity extends AppCompatActivity {

    private MaterialButton btnDateRange, btnSubmit;
    private TextView tvSelectedDateRange;
    private TextInputEditText etLocation;
    private TableLayout tableRecommendations;
    private String startDate, endDate;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spray_scheduling);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize UI components
        btnDateRange = findViewById(R.id.btnDateRange);
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange);
        etLocation = findViewById(R.id.etLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        tableRecommendations = findViewById(R.id.tableRecommendations);

        btnDateRange.setOnClickListener(v -> showDatePicker());
        btnSubmit.setOnClickListener(v -> saveSchedule());

        if (currentUser != null) {
            loadRecommendations(currentUser.getUid());
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select Date Range").build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startDate = sdf.format(new Date(selection.first));
            endDate = sdf.format(new Date(selection.second));
            tvSelectedDateRange.setText(startDate + " to " + endDate);
        });

        dateRangePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void saveSchedule() {
        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please select a date range", Toast.LENGTH_SHORT).show();
            return;
        }

        String location = etLocation.getText().toString().trim();
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String scheduleId = UUID.randomUUID().toString();

            Map<String, Object> schedule = new HashMap<>();
            schedule.put("startDate", startDate);
            schedule.put("endDate", endDate);
            schedule.put("location", location);

            mDatabase.child("users").child(userId)
                    .child("apple").child("sprayScheduling").child(scheduleId)
                    .setValue(schedule)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SpraySchedulingActivity.this, "Schedule saved", Toast.LENGTH_SHORT).show();

                        // 🔹 Add Fertilizer Recommendations (Can be changed to AI-based later)
                        addFertilizerRecommendation(userId, "Urea", "1 July - 10 July");
                        addFertilizerRecommendation(userId, "Potassium Sulfate", "15 July - 25 July");

                        // Load recommendations
                        loadRecommendations(userId);
                    });
        }
    }

    // 🔹 Method to add Fertilizer Recommendations to Firebase
    private void addFertilizerRecommendation(String userId, String fertilizerName, String bestDates) {
        DatabaseReference recommendationsRef = mDatabase.child("users").child(userId)
                .child("apple").child("sprayScheduling").child("recommendations");

        String recommendationId = UUID.randomUUID().toString();
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("fertilizerName", fertilizerName);
        recommendation.put("bestDates", bestDates);

        recommendationsRef.child(recommendationId).setValue(recommendation)
                .addOnSuccessListener(aVoid -> Toast.makeText(SpraySchedulingActivity.this,
                        "Fertilizer recommendation added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(SpraySchedulingActivity.this,
                        "Failed to add recommendation: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🔹 Load Recommendations from Firebase and display in Table
    private void loadRecommendations(String userId) {
        mDatabase.child("users").child(userId).child("apple").child("sprayScheduling")
                .child("recommendations")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Clear table before adding new rows
                        tableRecommendations.removeViews(1, Math.max(0, tableRecommendations.getChildCount() - 1));

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String fertilizerName = snapshot.child("fertilizerName").getValue(String.class);
                            String bestDates = snapshot.child("bestDates").getValue(String.class);

                            TableRow row = new TableRow(SpraySchedulingActivity.this);
                            row.setPadding(0, 8, 0, 8);

                            TextView tvFertilizer = new TextView(SpraySchedulingActivity.this);
                            tvFertilizer.setText(fertilizerName);
                            tvFertilizer.setPadding(8, 8, 8, 8);

                            TextView tvDates = new TextView(SpraySchedulingActivity.this);
                            tvDates.setText(bestDates);
                            tvDates.setPadding(8, 8, 8, 8);

                            row.addView(tvFertilizer);
                            row.addView(tvDates);

                            tableRecommendations.addView(row);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(SpraySchedulingActivity.this, "Failed to load recommendations", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
