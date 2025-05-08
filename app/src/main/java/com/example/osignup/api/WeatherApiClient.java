package com.example.osignup.api;

import com.example.osignup.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherApiClient {
    private static Retrofit retrofit = null;

    /**
     * Get the Retrofit client instance
     *
     * @return Retrofit instance
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Get the Weather API service instance
     *
     * @return WeatherApiService instance
     */
    public static WeatherApiService getWeatherApiService() {
        return getClient().create(WeatherApiService.class);
    }
}