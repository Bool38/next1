package com.example.osignup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.osignup.models.FirebaseWeatherData;
import com.example.osignup.utils.WeatherDataManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements WeatherDataManager.WeatherDataCallback {
    private static final String TAG = "WeatherActivity";

    // UI components
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvFeelsLike, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;
    private Button btnSearchCity, btnUseLocation;
    private EditText etCityInput;
    private TextInputLayout tilCityInput;
    private FloatingActionButton fabRefresh;

    // Data manager
    private WeatherDataManager weatherDataManager;
    private String lastSearchedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Weather Information");
        }

        // Initialize UI components
        initViews();

        // Initialize weather data manager
        weatherDataManager = new WeatherDataManager(this);
        weatherDataManager.setWeatherDataCallback(this);

        // Set click listeners
        setupListeners();

        // Fetch weather data for the user's saved location
        showLoading(true);
        weatherDataManager.fetchWeatherForUserLocation();
    }

    private void initViews() {
        tvCity = findViewById(R.id.tvCity);
        tvDate = findViewById(R.id.tvDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);
        btnSearchCity = findViewById(R.id.btnSearchCity);
        btnUseLocation = findViewById(R.id.btnUseLocation);
        etCityInput = findViewById(R.id.etCityInput);
        tilCityInput = findViewById(R.id.tilCityInput);
        fabRefresh = findViewById(R.id.fabRefresh);
    }

    private void setupListeners() {
        // Search button click listener
        btnSearchCity.setOnClickListener(v -> {
            String cityName = etCityInput.getText().toString().trim();
            if (!TextUtils.isEmpty(cityName)) {
                searchWeatherForCity(cityName);
            } else {
                tilCityInput.setError("Please enter a city name");
            }
        });

        // Use saved location button click listener
        btnUseLocation.setOnClickListener(v -> {
            showLoading(true);
            weatherDataManager.fetchWeatherForUserLocation();
        });

        // Refresh button click listener
        fabRefresh.setOnClickListener(v -> {
            if (lastSearchedCity != null && !lastSearchedCity.isEmpty()) {
                showLoading(true);
                weatherDataManager.fetchCurrentWeather(lastSearchedCity);
            } else {
                showLoading(true);
                weatherDataManager.fetchWeatherForUserLocation();
            }
        });

        // Handle keyboard "search" action
        etCityInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String cityName = etCityInput.getText().toString().trim();
                if (!TextUtils.isEmpty(cityName)) {
                    searchWeatherForCity(cityName);
                    return true;
                } else {
                    tilCityInput.setError("Please enter a city name");
                }
            }
            return false;
        });
    }

    private void searchWeatherForCity(String cityName) {
        showLoading(true);
        lastSearchedCity = cityName;
        weatherDataManager.fetchCurrentWeather(cityName);
        tilCityInput.setError(null);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        tvCity.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvDate.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvTemperature.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvDescription.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onWeatherDataFetched(FirebaseWeatherData weatherData) {
        showLoading(false);
        lastSearchedCity = weatherData.getCityName();

        // Update UI with weather data
        tvCity.setText(weatherData.getCityName());

        // Format date for better readability
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(weatherData.getDate());
            if (date != null) {
                tvDate.setText(outputFormat.format(date));
            } else {
                tvDate.setText(weatherData.getDate());
            }
        } catch (ParseException e) {
            tvDate.setText(weatherData.getDate());
        }

        tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", weatherData.getTemperature()));
        tvDescription.setText(capitalizeFirstLetter(weatherData.getWeatherDescription()));
        tvFeelsLike.setText(String.format(Locale.getDefault(), "%.1f°C", weatherData.getFeelsLike()));
        tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", weatherData.getHumidity()));
        tvWind.setText(String.format(Locale.getDefault(), "%.1f m/s", weatherData.getWindSpeed()));

        // Load weather icon
        String iconUrl = "https://openweathermap.org/img/wn/" + weatherData.getWeatherIcon() + "@2x.png";
        Glide.with(this)
                .load(iconUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(ivWeatherIcon);
    }

    @Override
    public void onWeatherDataFetchFailed(String errorMessage) {
        showLoading(false);
        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}