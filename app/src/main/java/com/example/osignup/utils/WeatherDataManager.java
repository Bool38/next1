package com.example.osignup.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.osignup.Constants;
import com.example.osignup.api.WeatherApiClient;
import com.example.osignup.api.WeatherApiService;
import com.example.osignup.models.FirebaseWeatherData;
import com.example.osignup.models.WeatherResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherDataManager {
    private static final String TAG = "WeatherDataManager";

    private final Context context;
    private final WeatherApiService weatherApiService;
    private final DatabaseReference mDatabase;
    private final FirebaseUser currentUser;

    private WeatherDataCallback weatherDataCallback;

    /**
     * Callback interface for weather data operations
     */
    public interface WeatherDataCallback {
        void onWeatherDataFetched(FirebaseWeatherData weatherData);
        void onWeatherDataFetchFailed(String errorMessage);
    }

    /**
     * Constructor
     *
     * @param context Application context
     */
    public WeatherDataManager(Context context) {
        this.context = context;
        this.weatherApiService = WeatherApiClient.getWeatherApiService();
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Set the callback for weather data operations
     *
     * @param callback The callback to set
     */
    public void setWeatherDataCallback(WeatherDataCallback callback) {
        this.weatherDataCallback = callback;
    }

    /**
     * Fetch current weather data by city name
     *
     * @param cityName The name of the city
     */
    public void fetchCurrentWeather(String cityName) {
        Log.d(TAG, "Fetching weather data for city: " + cityName);

        if (cityName == null || cityName.isEmpty()) {
            String errorMessage = "City name cannot be empty";
            Log.e(TAG, errorMessage);
            if (weatherDataCallback != null) {
                weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
            }
            return;
        }

        Call<WeatherResponse> call = weatherApiService.getCurrentWeatherData(
                cityName,
                Constants.API_KEY,
                Constants.UNITS
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    // Format the date
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String currentDate = sdf.format(new Date());

                    // Create FirebaseWeatherData object
                    FirebaseWeatherData firebaseWeatherData = new FirebaseWeatherData(weatherResponse, currentDate);

                    // Save to Firebase
                    saveWeatherDataToFirebase(firebaseWeatherData);

                    // Notify through callback
                    if (weatherDataCallback != null) {
                        weatherDataCallback.onWeatherDataFetched(firebaseWeatherData);
                    }

                    Log.d(TAG, "Weather data successfully fetched for: " + cityName);
                } else {
                    String errorMessage = "Failed to fetch weather data. " +
                            "Error code: " + response.code();

                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ". Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Log.e(TAG, errorMessage);

                    if (weatherDataCallback != null) {
                        weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);

                if (weatherDataCallback != null) {
                    weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
                }
            }
        });
    }

    /**
     * Save weather data to Firebase database
     *
     * @param weatherData The weather data to save
     */
    private void saveWeatherDataToFirebase(FirebaseWeatherData weatherData) {
        if (currentUser == null) {
            Log.e(TAG, "No user is signed in");
            return;
        }

        String userId = currentUser.getUid();
        String cityName = weatherData.getCityName().toLowerCase().replace(" ", "_");

        // Create a unique key for this weather data entry
        String weatherEntryId = mDatabase.child("users").child(userId)
                .child("weather").child(cityName).push().getKey();

        if (weatherEntryId != null) {
            mDatabase.child("users").child(userId)
                    .child("weather").child(cityName).child(weatherEntryId)
                    .setValue(weatherData.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Weather data saved to Firebase");
                        Toast.makeText(context, "Weather data saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to save weather data to Firebase", e);
                        Toast.makeText(context, "Failed to save weather data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

            // Also update the latest weather data for quick access
            mDatabase.child("users").child(userId)
                    .child("weather").child(cityName).child("latest")
                    .setValue(weatherData.toMap())
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update latest weather data", e));
        } else {
            Log.e(TAG, "Failed to create key for weather data");
        }
    }

    /**
     * Fetch current weather data using stored location for the current user
     */
    public void fetchWeatherForUserLocation() {
        if (currentUser == null) {
            Log.e(TAG, "No user is signed in");
            return;
        }

        String userId = currentUser.getUid();

        // Get the location from the user's spray scheduling data
        mDatabase.child("users").child(userId)
                .child("apple").child("sprayScheduling")
                .limitToLast(1)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String location = snapshot.child("location").getValue(String.class);
                            if (location != null && !location.isEmpty()) {
                                Log.d(TAG, "Found location: " + location);
                                fetchCurrentWeather(location);
                            } else {
                                String errorMessage = "Location not found in user data";
                                Log.e(TAG, errorMessage);
                                if (weatherDataCallback != null) {
                                    weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
                                }
                            }
                            break; // Just get the first (most recent) entry
                        }
                    } else {
                        String errorMessage = "No spray scheduling data found for user";
                        Log.e(TAG, errorMessage);
                        if (weatherDataCallback != null) {
                            weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Failed to retrieve location: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    if (weatherDataCallback != null) {
                        weatherDataCallback.onWeatherDataFetchFailed(errorMessage);
                    }
                });
    }
}