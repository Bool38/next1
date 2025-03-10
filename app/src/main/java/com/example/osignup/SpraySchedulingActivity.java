package com.example.osignup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpraySchedulingActivity extends AppCompatActivity {

    private TextView tvStartDate, tvEndDate;
    private ImageView ivStartDatePicker, ivEndDatePicker;
    private Spinner spinnerLocation;
    private Button btnSave;
    private Toolbar toolbar;

    private Calendar startDateCalendar, endDateCalendar;
    private SimpleDateFormat dateFormat;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spray_scheduling);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize date format
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        ivStartDatePicker = findViewById(R.id.ivStartDatePicker);
        ivEndDatePicker = findViewById(R.id.ivEndDatePicker);
        spinnerLocation = findViewById(R.id.spinnerLocation);
        btnSave = findViewById(R.id.btnSave);

        // Set up location spinner
        setupLocationSpinner();

        // Set up date pickers
        ivStartDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(true);
            }
        });

        ivEndDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(false);
            }
        });

        // Set up save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveScheduleData();
            }
        });
    }

    private void setupLocationSpinner() {
        // Create an array of locations
        String[] locations = new String[] {
                "Select Location", "North Orchard", "South Orchard", "East Farm", "West Farm", "Central Garden"
        };

        // Create adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, locations);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLocation.setAdapter(adapter);
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        Calendar calendar = isStartDate ? startDateCalendar : endDateCalendar;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (isStartDate) {
                            startDateCalendar.set(Calendar.YEAR, year);
                            startDateCalendar.set(Calendar.MONTH, month);
                            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tvStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
                        } else {
                            endDateCalendar.set(Calendar.YEAR, year);
                            endDateCalendar.set(Calendar.MONTH, month);
                            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tvEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void saveScheduleData() {
        // Validate inputs
        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();
        String location = spinnerLocation.getSelectedItem().toString();

        if (startDate.equals("Select date")) {
            Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.equals("Select date")) {
            Toast.makeText(this, "Please select an end date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.equals("Select Location")) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if end date is after start date
        if (endDateCalendar.before(startDateCalendar)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Please log in to save data", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String email = currentUser.getEmail();

        // Create data object
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("userId", userId);
        scheduleData.put("email", email);
        scheduleData.put("startDate", startDate);
        scheduleData.put("endDate", endDate);
        scheduleData.put("location", location);
        scheduleData.put("createdAt", Calendar.getInstance().getTimeInMillis());

        // Save to Firebase
        String scheduleId = mDatabase.child("spray_schedules").push().getKey();

        mDatabase.child("spray_schedules").child(scheduleId).setValue(scheduleData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SpraySchedulingActivity.this,
                                    "Schedule saved successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to dashboard
                        } else {
                            Toast.makeText(SpraySchedulingActivity.this,
                                    "Failed to save schedule: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Handle back button in toolbar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}