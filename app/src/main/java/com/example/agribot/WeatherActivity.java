package com.example.agribot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private Button return4;

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String CHANNEL_ID = "weather_updates";
    private TextView textTemperature, textHumidity, textWeatherCondition;
    private ImageView weatherIcon;
    private Button buttonRefresh;
    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private ArrayList<String> forecastList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private final String API_KEY = "cb072a4f8ba03551bce62e14aff0924b"; // Replace with your API key
    private String lastCondition = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        return4 = findViewById(R.id.return4);
        return4.setOnClickListener(v -> {
            Intent intent = new Intent(WeatherActivity.this, MainActivity2.class);
            startActivity(intent);
        });

        textTemperature = findViewById(R.id.textTemperature);
        textHumidity = findViewById(R.id.textHumidity);
        textWeatherCondition = findViewById(R.id.textWeatherCondition);
        weatherIcon = findViewById(R.id.weatherIcon);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastAdapter = new ForecastAdapter(forecastList);
        forecastRecyclerView.setAdapter(forecastAdapter);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannel();
        buttonRefresh.setOnClickListener(v -> getCurrentLocation());
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    fetchWeatherData(location.getLatitude(), location.getLongitude());
                    fetchForecastData(location.getLatitude(), location.getLongitude());
                }
            }
        });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray weatherArray = json.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String condition = weather.getString("description");
                        JSONObject main = json.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        int humid = main.getInt("humidity");

                        runOnUiThread(() -> {
                            textWeatherCondition.setText("Condition: " + condition);
                            textTemperature.setText("Temperature: " + temp + "°C");
                            textHumidity.setText("Humidity: " + humid + "%");
                            if (!condition.equals(lastCondition)) {
                                sendWeatherNotification(condition);
                                lastCondition = condition;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fetchForecastData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray list = json.getJSONArray("list");

                        forecastList.clear();
                        for (int i = 0; i < 5; i++) {
                            JSONObject forecast = list.getJSONObject(i);
                            double temp = forecast.getJSONObject("main").getDouble("temp");
                            String desc = forecast.getJSONArray("weather").getJSONObject(0).getString("description");
                            forecastList.add("Day " + (i + 1) + ": " + temp + "°C, " + desc);
                        }

                        runOnUiThread(() -> forecastAdapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Weather Updates", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void sendWeatherNotification(String condition) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.free_vector_weather_symbols_clip_art_109957_weather_symbols_clip_art_hight)
                .setContentTitle("Weather Update")
                .setContentText("New Condition: " + condition)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}