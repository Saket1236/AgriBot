package com.example.agribot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Arrays;
import java.util.List;

public class OwnDairy extends AppCompatActivity {

    private static final String CHANNEL_ID = "Agribot_Notifications";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private GeminiApiHelper geminiApiHelper;
    private EditText userInput;
    private Button sendButton, returnButton;
    private TextView responseText;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    private String locationDetails = "Location not available";

    private final List<String> dairyKeywords = Arrays.asList(
            "milk", "dairy", "cattle", "cow", "buffalo", "dairy farm", "cheese", "butter", "cream", "yogurt",
            "pasteurization", "dairy processing", "milk production", "udder", "lactation", "dairy cows",
            "dairy industry", "farm fresh milk", "milk storage", "homogenization", "milking machine",
            "ghee", "curd", "dairy farming", "dairy products", "organic milk", "milk quality", "milk yield",
            "milking parlor", "milk tanker", "dairy cooperative", "dairy supply chain", "dairy herd",
            "fodder", "silage", "feed management", "nutrition for dairy cows", "veterinary care", "calf",
            "artificial insemination", "dairy genetics", "milk fat", "milk protein", "buttermilk",
            "churning", "cheese production", "fermented dairy", "probiotic dairy", "dairy cattle breeds",
            "grazing", "milk testing", "cold storage", "milk pasteurization", "ultra-pasteurization",
            "dairy waste management", "whey", "casein", "dairy hygiene", "livestock management",
            "milk processing plant", "bulk milk cooler", "dairy automation", "milk vending machine",
            "cream separator", "milk analyzer", "udder infection", "mastitis", "bovine health",
            "reproductive health of dairy cattle", "cattle feed", "ruminant nutrition", "barn management",
            "cow comfort", "milk market", "value-added dairy products", "skimming", "full cream milk",
            "low-fat milk", "milk substitutes", "milk fortification", "dairy business", "dairy economics",
            "dairy sustainability", "dairy entrepreneurship", "organic dairy", "milk processing unit",
            "dairy farm infrastructure", "hoof care", "cattle vaccination", "milk packaging", "dairy feed",
            "small-scale dairy farming", "large-scale dairy production", "dairy technology",
            "dairy equipment", "dairy supply management", "farm-to-table dairy", "lactose", "lactose-free milk",
            "dairy cow breeds", "milking routine", "dairy refrigeration", "cattle sheds", "barn ventilation",
            "udder care", "bovine nutrition", "dairy science", "dairy feed supplements", "cattle housing",
            "dairy business plan", "dairy farm economics", "commercial dairy farming", "dairy research",
            "government schemes for dairy farming", "milk safety standards", "milk chilling", "milk adulteration testing"
    );


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_dairy);

        userInput = findViewById(R.id.editTextPrompt2);
        sendButton = findViewById(R.id.buttonSend2);
        responseText = findViewById(R.id.textViewResponse2);
        progressBar = findViewById(R.id.progressBar2);
        returnButton = findViewById(R.id.return2);

        geminiApiHelper = new GeminiApiHelper();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();
        requestLocationPermission();

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(OwnDairy.this, farming.class);
            startActivity(intent);
        });

        sendButton.setOnClickListener(v -> handleSendButtonClick());
    }

    private void handleSendButtonClick() {
        String prompt = userInput.getText().toString().toLowerCase().trim();
        if (prompt.isEmpty()) {
            responseText.setText("Please enter a valid query.");
            return;
        }

        if (!isFarmingRelated(prompt)) {
            responseText.setText("Invalid question. Please ask Dairy-related queries.");
            return;
        }

        sendButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        responseText.setText("Fetching response...");
        getUserLocationAndFetchResponse(prompt);
    }

    private boolean isFarmingRelated(String prompt) {
        for (String keyword : dairyKeywords) {
            if (prompt.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void getUserLocationAndFetchResponse(String question) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationDetails = "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude();
                                Toast.makeText(OwnDairy.this, locationDetails, Toast.LENGTH_LONG).show();
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
                    responseText.setText("Response: " + response);
                    sendNotification("Agribot", "Your AI-generated answer is ready!");
                    resetUI();
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    responseText.setText("Error: " + error);
                    resetUI();
                });
            }
        });
    }

    private void resetUI() {
        sendButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Agribot Notifications";
            String description = "Notifications for answer updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, OwnDairy.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.picsart_25_03_18_22_02_30_502)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }
}
