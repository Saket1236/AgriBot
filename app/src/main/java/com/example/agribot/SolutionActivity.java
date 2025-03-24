package com.example.agribot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class SolutionActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "Agribot_Notification_Channel";

    private Button return3, btnTranslate;
    private GeminiApiHelper geminiApiHelper;
    private TextView questionText, solutionText;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private String locationDetails = "Location not available";
    private String originalSolution = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        return3 = findViewById(R.id.return3);
        btnTranslate = findViewById(R.id.btnTranslate);
        questionText = findViewById(R.id.textViewQuestion);
        solutionText = findViewById(R.id.textViewSolution);
        progressBar = findViewById(R.id.progressBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geminiApiHelper = new GeminiApiHelper();
        createNotificationChannel();
        requestLocationPermission();

        return3.setOnClickListener(v -> {
            Intent intent = new Intent(SolutionActivity.this, farming.class);
            startActivity(intent);
            finish();
        });

        String question = getIntent().getStringExtra("QUESTION");
        if (question != null) {
            questionText.setText(question);
            progressBar.setVisibility(View.VISIBLE);
            solutionText.setText("");

            getUserLocationAndFetchResponse(question);
        } else {
            solutionText.setText("No question received.");
        }

        // Translate Button Click
        btnTranslate.setOnClickListener(v -> translateText(originalSolution, TranslateLanguage.HINDI)); // Change to any language
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocationAndFetchResponse(String question) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationDetails = "Latitude: " + location.getLatitude() +
                                        ", Longitude: " + location.getLongitude();
                            }
                            fetchGeminiResponse(question);
                        }
                    });
        } else {
            fetchGeminiResponse(question);
        }
    }

    private void fetchGeminiResponse(String question) {
        String questionWithLocation = question + "\nUser Location: " + locationDetails;

        geminiApiHelper.generateResponse(questionWithLocation, new GeminiApiHelper.GeminiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    originalSolution = response;
                    solutionText.setText(response);
                    sendNotification("Agribot Response", "Your AI-generated answer is ready!");
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    solutionText.setText("Error: " + error);
                });
            }
        });
    }

    private void translateText(String text, String targetLanguage) {
        if (text.isEmpty()) {
            Toast.makeText(this, "No text to translate", Toast.LENGTH_SHORT).show();
            return;
        }

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(targetLanguage)
                        .build();

        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused ->
                        translator.translate(text)
                                .addOnSuccessListener(translatedText -> solutionText.setText(translatedText))
                                .addOnFailureListener(e -> Toast.makeText(SolutionActivity.this, "Translation failed", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(SolutionActivity.this, "Translation model download failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocationAndFetchResponse(questionText.getText().toString());
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                fetchGeminiResponse(questionText.getText().toString());
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Agribot Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String content) {
        Intent intent = new Intent(this, SolutionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.picsart_25_03_18_22_02_30_502)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }
}
