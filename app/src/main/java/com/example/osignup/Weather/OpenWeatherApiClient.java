package com.example.osignup.Weather;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OpenWeatherApiClient {
    private static final String TAG = "OpenWeatherApiClient";
    private static final String API_KEY = "fd9246a35032afc4874e611d1bb7dbe3";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";

    private final RequestQueue requestQueue;
    private final Gson gson;

    public interface WeatherForecastCallback {
        void onSuccess(WeatherForecast forecast);
        void onError(String errorMessage);
    }

    public OpenWeatherApiClient(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }

    public void getWeatherForecast(String location, String startDate, String endDate, WeatherForecastCallback callback) {
        // Create URL for 5-day forecast (max allowed by free API)
        String url = String.format(Locale.US,
                "%s?q=%s&units=metric&appid=%s",
                BASE_URL, location, API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            WeatherForecast forecast = parseWeatherData(response, location, startDate, endDate);
                            callback.onSuccess(forecast);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            callback.onError("Failed to parse weather data: " + e.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing weather data: " + e.getMessage());
                            callback.onError("Error processing weather data: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "API Error: " + error.toString());
                        callback.onError("Failed to fetch weather data: " + error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    private WeatherForecast parseWeatherData(JSONObject response, String location,
                                             String startDate, String endDate) throws JSONException {
        WeatherForecast forecast = new WeatherForecast(location, startDate, endDate);

        // Extract city data
        JSONObject city = response.getJSONObject("city");
        String cityName = city.getString("name");

        // Parse list of forecasts
        JSONArray forecastList = response.getJSONArray("list");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            Date start = inputFormat.parse(startDate);
            Calendar calendar = Calendar.getInstance();

            if (start != null) {
                calendar.setTime(start);

                // We only need 10 days of data starting from startDate
                int daysCount = 0;
                int maxDays = 10;

                for (int i = 0; i < forecastList.length() && daysCount < maxDays; i++) {
                    JSONObject forecastItem = forecastList.getJSONObject(i);
                    String dtTxt = forecastItem.getString("dt_txt");

                    // Format date to match our desired format (YYYY-MM-DD)
                    String forecastDate = dtTxt.split(" ")[0];
                    Date forecastDateObj = inputFormat.parse(forecastDate);

                    if (forecastDateObj != null && !forecastDateObj.before(start)) {
                        // Only process if we haven't seen this date before
                        String formattedDate = outputFormat.format(forecastDateObj);

                        if (forecast.getDailyForecasts().containsKey(formattedDate)) {
                            continue; // Skip duplicate dates
                        }

                        // Extract main weather data
                        JSONObject main = forecastItem.getJSONObject("main");
                        double temperature = main.getDouble("temp");
                        double humidity = main.getDouble("humidity");

                        // Extract wind data
                        JSONObject wind = forecastItem.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");

                        // Extract weather conditions
                        JSONArray weatherArray = forecastItem.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String condition = weather.getString("main");
                        String description = weather.getString("description");
                        String icon = weather.getString("icon");

                        // Extract precipitation (rain/snow if available)
                        double precipitation = 0.0;
                        if (forecastItem.has("rain") && forecastItem.getJSONObject("rain").has("3h")) {
                            precipitation = forecastItem.getJSONObject("rain").getDouble("3h");
                        } else if (forecastItem.has("snow") && forecastItem.getJSONObject("snow").has("3h")) {
                            precipitation = forecastItem.getJSONObject("snow").getDouble("3h");
                        }

                        // Create and add WeatherData
                        WeatherData weatherData = new WeatherData(
                                formattedDate, temperature, humidity, windSpeed,
                                condition, description, icon, precipitation);

                        forecast.addDailyForecast(formattedDate, weatherData);
                        daysCount++;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Date parsing error: " + e.getMessage());
            throw new JSONException("Error parsing dates: " + e.getMessage());
        }

        return forecast;
    }
}