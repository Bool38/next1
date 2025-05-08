package com.example.osignup.models;

import java.util.HashMap;
import java.util.Map;

public class FirebaseWeatherData {
    private String cityName;
    private float temperature;
    private float feelsLike;
    private float tempMin;
    private float tempMax;
    private int humidity;
    private int pressure;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private float windSpeed;
    private int windDegree;
    private long timestamp;
    private String date;

    public FirebaseWeatherData() {
        // Required empty constructor for Firebase
    }

    public FirebaseWeatherData(WeatherResponse response, String date) {
        this.cityName = response.getCityName();
        this.temperature = response.getMain().getTemperature();
        this.feelsLike = response.getMain().getFeelsLike();
        this.tempMin = response.getMain().getTempMin();
        this.tempMax = response.getMain().getTempMax();
        this.humidity = response.getMain().getHumidity();
        this.pressure = response.getMain().getPressure();

        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            Weather weather = response.getWeather().get(0);
            this.weatherMain = weather.getMain();
            this.weatherDescription = weather.getDescription();
            this.weatherIcon = weather.getIcon();
        }

        this.windSpeed = response.getWind().getSpeed();
        this.windDegree = response.getWind().getDegree();
        this.timestamp = response.getDateTime();
        this.date = date;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cityName", cityName);
        map.put("temperature", temperature);
        map.put("feelsLike", feelsLike);
        map.put("tempMin", tempMin);
        map.put("tempMax", tempMax);
        map.put("humidity", humidity);
        map.put("pressure", pressure);
        map.put("weatherMain", weatherMain);
        map.put("weatherDescription", weatherDescription);
        map.put("weatherIcon", weatherIcon);
        map.put("windSpeed", windSpeed);
        map.put("windDegree", windDegree);
        map.put("timestamp", timestamp);
        map.put("date", date);
        return map;
    }

    // Getters and setters
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(float feelsLike) {
        this.feelsLike = feelsLike;
    }

    public float getTempMin() {
        return tempMin;
    }

    public void setTempMin(float tempMin) {
        this.tempMin = tempMin;
    }

    public float getTempMax() {
        return tempMax;
    }

    public void setTempMax(float tempMax) {
        this.tempMax = tempMax;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(int windDegree) {
        this.windDegree = windDegree;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}