package com.example.loginapp2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextLocation;
    private Spinner spinnerCropType;
    private Button buttonLogin;

    private String selectedCropType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextLocation = findViewById(R.id.editTextLocation);
        spinnerCropType = findViewById(R.id.spinnerCropType);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Setup crop type spinner
        setupCropTypeSpinner();

        // Set up login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });
    }

    private void setupCropTypeSpinner() {
        // Create an ArrayAdapter using a simple spinner layout and crop types array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.crop_types,
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerCropType.setAdapter(adapter);

        // Set listener for item selection
        spinnerCropType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCropType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCropType = null;
            }
        });
    }

    private void validateAndLogin() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (!isValidEmail(email)) {
            editTextEmail.setError("Invalid email format");
            return;
        }

        if (location.isEmpty()) {
            editTextLocation.setError("Location is required");
            return;
        }

        if (selectedCropType == null || selectedCropType.equals("Select Crop Type")) {
            Toast.makeText(this, "Please select a crop type", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all validations pass, proceed with login
        // Here you would typically connect to your backend or handle authentication

        // For demo purposes, just show success message and user details
        String message = "Login Successful!\n" +
                "Username: " + username + "\n" +
                "Email: " + email + "\n" +
                "Crop Type: " + selectedCropType + "\n" +
                "Location: " + location;

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Navigate to main activity or dashboard
        // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // startActivity(intent);
        // finish();
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
}