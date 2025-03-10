package com.example.loginapp2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp2.R;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button button;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        textView = findViewById(R.id.textViewCropType);
        button = findViewById(R.id.buttonLogin);

        // Set initial text
        textView.setText("Counter: " + counter);

        // Set up button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                textView.setText("Counter: " + counter);
                Toast.makeText(MainActivity.this, "Button clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Code to execute when activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Code to execute when activity is paused
    }
}