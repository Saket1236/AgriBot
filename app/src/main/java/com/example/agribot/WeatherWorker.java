package com.example.agribot;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherWorker extends Worker {
    private static final String API_KEY = "cb072a4f8ba03551bce62e14aff0924b"; // Replace with your API key
    private static final String CITY = "Nashik";
    private static final String CHANNEL_ID = "weather_channel";

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&appid=" + API_KEY + "&units=metric";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                JSONObject json = new JSONObject(responseData);
                JSONObject main = json.getJSONObject("main");
                double temp = main.getDouble("temp");
                int humidity = main.getInt("humidity");
                JSONArray weatherArray = json.getJSONArray("weather");
                String condition = weatherArray.getJSONObject(0).getString("description");

                String weatherMessage = "🌤 " + condition + "\n🌡 Temp: " + temp + "°C\n💧 Humidity: " + humidity + "%";

                sendNotification(weatherMessage);
            }
        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }

        return Result.success();
    }

    @SuppressLint("MissingPermission")
    private void sendNotification(String message) {
        Context context = getApplicationContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Weather Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Weather notifications every few hours");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.free_vector_weather_symbols_clip_art_109957_weather_symbols_clip_art_hight) // Add a weather icon in res/drawable
                .setContentTitle("Weather Update")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
