package com.example.agribot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class farming extends AppCompatActivity {
    private Button return1, own, Q1, Q2, Q3, Q4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farming);

        return1 = findViewById(R.id.return1);
        own = findViewById(R.id.own);
        Q1 = findViewById(R.id.Q1);
        Q2 = findViewById(R.id.Q2);
        Q3 = findViewById(R.id.Q3);
        Q4 = findViewById(R.id.Q4);

        return1.setOnClickListener(v -> {
            Intent intent = new Intent(farming.this, MainActivity2.class);
            startActivity(intent);
            finish(); // Optional: Close current activity
        });

        own.setOnClickListener(v -> {
            Intent intent = new Intent(farming.this, ownquestion.class);
            startActivity(intent);
        });

        Q1.setOnClickListener(v -> openSolutionActivity("What is the best time to plant wheat in my region?"));
        Q2.setOnClickListener(v -> openSolutionActivity("How can I improve soil fertility for better crop yield?"));
        Q3.setOnClickListener(v -> openSolutionActivity("Which organic fertilizers are best for vegetable farming?"));
        Q4.setOnClickListener(v -> openSolutionActivity("How do I prevent fungal infections in crops?"));
    }

    private void openSolutionActivity(String question) {
        Intent intent = new Intent(farming.this, SolutionActivity.class);
        intent.putExtra("QUESTION", question); // ✅ Fixed key name
        startActivity(intent);
    }
}
