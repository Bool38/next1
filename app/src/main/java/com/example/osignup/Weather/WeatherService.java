package com.example.osignup.Weather;

import android.content.Context;
import android.util.Log;

public class WeatherService {
    private static final String TAG = "WeatherService";
    private final OpenWeatherApiClient apiClient;
    private final WeatherRepository repository;

    public interface WeatherServiceCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public WeatherService(Context context) {
        this.apiClient = new OpenWeatherApiClient(context);
        this.repository = new WeatherRepository();
    }

    public void fetchAndStoreWeatherData(String location, String startDate, String endDate, WeatherServiceCallback callback) {
        Log.d(TAG, "Fetching weather data for location: " + location);

        // First check if we already have this data
        repository.getWeatherForecast(location, new WeatherRepository.WeatherForecastCallback() {
            @Override
            public void onForecastLoaded(WeatherForecast forecast) {
                // We already have the data, no need to fetch again
                Log.d(TAG, "Weather data already exists for this location");
                callback.onSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                // Data doesn't exist or there was an error, fetch from API
                fetchFromApi(location, startDate, endDate, callback);
            }
        });
    }

    private void fetchFromApi(String location, String startDate, String endDate, WeatherServiceCallback callback) {
        apiClient.getWeatherForecast(location, startDate, endDate, new OpenWeatherApiClient.WeatherForecastCallback() {
            @Override
            public void onSuccess(WeatherForecast forecast) {
                // Store the fetched data in Firebase
                repository.saveWeatherForecast(forecast, new WeatherRepository.WeatherRepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Weather data fetched and stored successfully");
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to store weather data: " + errorMessage);
                        callback.onError("Failed to store weather data: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch weather data: " + errorMessage);
                callback.onError("Failed to fetch weather data: " + errorMessage);
            }
        });
    }
}