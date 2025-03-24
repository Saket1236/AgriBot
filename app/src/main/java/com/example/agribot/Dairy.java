package com.example.agribot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Dairy extends AppCompatActivity {

    private Button return5, own2, q1, q2, q3, q4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy);

        // Initializing buttons
        return5 = findViewById(R.id.return5);
        own2 = findViewById(R.id.own2);
        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);

        // Setting up button click listeners
        return5.setOnClickListener(v -> startActivity(new Intent(Dairy.this, MainActivity2.class)));
        own2.setOnClickListener(v -> startActivity(new Intent(Dairy.this, OwnDairy.class)));

        // Set listeners for Q&A buttons
        q1.setOnClickListener(v -> openSolution("How can I increase milk production naturally?"));
        q2.setOnClickListener(v -> openSolution("What are the signs of a healthy cow?"));
        q3.setOnClickListener(v -> openSolution("How do I protect my cows from heat stress?"));
        q4.setOnClickListener(v -> openSolution("What should I do if my cow stops eating?"));
    }

    private void openSolution(String question) {
        Intent intent = new Intent(Dairy.this, DairySolutions.class);
        intent.putExtra("question", question);
        startActivity(intent);
    }
}
