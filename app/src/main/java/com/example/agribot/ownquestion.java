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
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Arrays;
import java.util.List;

public class ownquestion extends AppCompatActivity {

    private static final String CHANNEL_ID = "Agribot_Notifications";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private GeminiApiHelper geminiApiHelper;
    private Button return2;
    private ProgressBar progressBar;
    private TextView responseText;
    private EditText userInput;
    private FusedLocationProviderClient fusedLocationClient;
    private String locationDetails = "Location not available";

    private final List<String> farmingKeywords = Arrays.asList(
            "crop", "irrigation", "pest", "soil", "fertilizer", "harvest", "weather", "farming", "farmers",
            "agriculture", "seeds", "organic", "watering", "livestock", "climate", "plant", "yield",
            "disease", "farm tools", "drought", "pesticide", "compost", "greenhouse", "grow", "Grow",
            "mulching", "crop rotation", "paddy", "horticulture", "vermicompost", "manure", "weed",
            "tractor", "cultivation", "drip irrigation", "green manure", "seedling", "germination",
            "hydroponics", "aeroponics", "silage", "fodder", "grazing", "barn", "tillage", "no-till farming",
            "organic farming", "sustainable farming", "precision farming", "integrated pest management",
            "cover crops", "agroforestry", "agronomy", "soil health", "crop disease", "farm management",
            "weather forecast", "micro-irrigation", "biodynamic farming", "pollination", "bee farming",
            "agrochemicals", "herbicide", "fungicide", "seed treatment", "green revolution", "hybrid seeds",
            "crop insurance", "food security", "cash crops", "dairy farming", "poultry farming",
            "aquaponics", "permaculture", "climate-smart agriculture", "biofertilizer", "carbon farming",
            "regenerative agriculture", "genetically modified crops", "pest control", "cover cropping",
            "precision irrigation", "crop productivity", "organic compost", "silo", "farm mechanization",
            "pasture", "soil fertility", "tillage methods", "water conservation", "sustainable agriculture",
            "eco-friendly farming", "vertical farming", "rain-fed agriculture", "agriculture technology",
            "ranching", "land cultivation", "seed germination", "row planting", "sprinkler irrigation",
            "farmland", "subsistence farming", "horticultural crops", "seasonal crops", "harvesting techniques",
            "field crops", "fallow land", "farmer cooperative", "disease-resistant crops", "irrigation system",
            "livestock management", "milk production", "farming subsidy", "biochar", "crop residue",
            "soil erosion control", "soil conservation", "nutrient management", "sustainable food production"
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ownquestion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        return2 = findViewById(R.id.return2);
        userInput = findViewById(R.id.editTextPrompt);
        Button sendButton = findViewById(R.id.buttonSend);
        responseText = findViewById(R.id.textViewResponse);
        progressBar = findViewById(R.id.progressBar);

        geminiApiHelper = new GeminiApiHelper();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create Notification Channel (For Android 8+)
        createNotificationChannel();

        // Request location permissions
        requestLocationPermission();

        return2.setOnClickListener(v -> {
            Intent intent = new Intent(ownquestion.this, farming.class);
            startActivity(intent);
        });

        sendButton.setOnClickListener(v -> {
            String prompt = userInput.getText().toString().toLowerCase();
            if (!prompt.isEmpty()) {
                if (isFarmingRelated(prompt)) {
                    progressBar.setVisibility(View.VISIBLE);
                    responseText.setText("");

                    // Fetch user location and then get AI response
                    getUserLocationAndFetchResponse(prompt);
                } else {
                    responseText.setText("Invalid question. Please ask farming-related queries.");
                }
            }
        });
    }

    // Function to check if the question is related to farming
    private boolean isFarmingRelated(String prompt) {
        for (String keyword : farmingKeywords) {
            if (prompt.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Request Location Permission
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Fetch User Location and Call Gemini API
    @SuppressLint("MissingPermission")
    private void getUserLocationAndFetchResponse(String question) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationDetails = "Latitude: " + location.getLatitude() +
                                        ", Longitude: " + location.getLongitude();
                                Toast.makeText(ownquestion.this, locationDetails, Toast.LENGTH_LONG).show();
                            }
                            // Fetch AI Response after getting location
                            fetchGeminiResponse(question);
                        }
                    });
        } else {
            // Fetch AI Response even if location is not available
            fetchGeminiResponse(question);
        }
    }

    // Fetch AI Response from Gemini API
    private void fetchGeminiResponse(String question) {
        String questionWithLocation = question + "\nUser Location: " + locationDetails;

        geminiApiHelper.generateResponse(questionWithLocation, new GeminiApiHelper.GeminiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    responseText.setText(response);
                    sendNotification("Agribot", "Your AI-generated answer is ready!");
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    responseText.setText("Error: " + error);
                });
            }
        });
    }

    // Handle Permission Request Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted - Fetch Location
                getUserLocationAndFetchResponse(userInput.getText().toString());
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                fetchGeminiResponse(userInput.getText().toString()); // Proceed without location
            }
        }
    }

    // Create Notification Channel (For Android 8+)
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

    // Send Notification After Answer is Generated
    @SuppressLint("MissingPermission")
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, ownquestion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
