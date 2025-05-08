package com.example.osignup.Weather;

public class WeatherData {
    private String date;
    private double temperature;
    private double humidity;
    private double windSpeed;
    private String weatherCondition;
    private String weatherDescription;
    private String icon;
    private double precipitation;

    // Default constructor for Firebase
    public WeatherData() {
    }

    // Constructor with parameters
    public WeatherData(String date, double temperature, double humidity, double windSpeed,
                       String weatherCondition, String weatherDescription, String icon,
                       double precipitation) {
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCondition = weatherCondition;
        this.weatherDescription = weatherDescription;
        this.icon = icon;
        this.precipitation = precipitation;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
}