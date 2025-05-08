package com.example.osignup.Weather;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WeatherForecast {
    private String location;
    private String startDate;
    private String endDate;
    private Map<String, WeatherData> dailyForecasts;

    // Default constructor for Firebase
    public WeatherForecast() {
        dailyForecasts = new HashMap<>();
    }

    // Constructor with parameters
    public WeatherForecast(String location, String startDate, String endDate) {
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyForecasts = new HashMap<>();
    }

    // Add a daily forecast
    public void addDailyForecast(String date, WeatherData weatherData) {
        if (dailyForecasts == null) {
            dailyForecasts = new HashMap<>();
        }
        dailyForecasts.put(date, weatherData);
    }

    // Getters and setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Map<String, WeatherData> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(Map<String, WeatherData> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }
}