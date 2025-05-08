package com.example.osignup.api;

import com.example.osignup.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    /**
     * Get current weather data by city name
     *
     * @param cityName Name of the city
     * @param apiKey Your API key
     * @param units Unit system (metric, imperial, standard)
     * @return Call object with WeatherResponse
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherData(
            @Query("q") String cityName,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    /**
     * Get current weather data by latitude and longitude
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param apiKey Your API key
     * @param units Unit system (metric, imperial, standard)
     * @return Call object with WeatherResponse
     */
    @GET("weather")
    Call<WeatherResponse> getCurrentWeatherDataByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}