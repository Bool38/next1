package com.example.osignup.Weather;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class WeatherRepository {
    private static final String TAG = "WeatherRepository";
    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public interface WeatherRepositoryCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface WeatherForecastCallback {
        void onForecastLoaded(WeatherForecast forecast);
        void onError(String errorMessage);
    }

    public WeatherRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void saveWeatherForecast(WeatherForecast forecast, WeatherRepositoryCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String locationKey = forecast.getLocation().toLowerCase().replace(" ", "_");
        String path = String.format("users/%s/weather_data/%s", userId, locationKey);

        mDatabase.child(path)
                .setValue(forecast)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Weather forecast saved successfully");
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to save weather forecast: " + e.getMessage());
                        callback.onError("Failed to save weather data: " + e.getMessage());
                    }
                });
    }

    public void getWeatherForecast(String location, WeatherForecastCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String locationKey = location.toLowerCase().replace(" ", "_");
        String path = String.format("users/%s/weather_data/%s", userId, locationKey);

        mDatabase.child(path)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            WeatherForecast forecast = dataSnapshot.getValue(WeatherForecast.class);
                            if (forecast != null) {
                                Log.d(TAG, "Weather forecast loaded successfully");
                                callback.onForecastLoaded(forecast);
                            } else {
                                callback.onError("Failed to parse weather data");
                            }
                        } else {
                            callback.onError("No weather data found for this location");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to load weather data: " + databaseError.getMessage());
                        callback.onError("Failed to load weather data: " + databaseError.getMessage());
                    }
                });
    }
}