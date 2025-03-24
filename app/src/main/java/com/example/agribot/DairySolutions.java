package com.example.agribot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class DairySolutions extends AppCompatActivity {

    private TextView questionText, answerText;
    private Button backButton, translateButton;
    private ProgressBar progressBar;
    private GeminiApiHelper geminiApiHelper = new GeminiApiHelper(); // Initialize Gemini API Helper

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dairy_solutions);

        // Initializing UI elements
        questionText = findViewById(R.id.textViewQuestion);
        answerText = findViewById(R.id.textViewSolution);
        backButton = findViewById(R.id.return3);
        translateButton = findViewById(R.id.btnTranslate2); // New Translate Button
        progressBar = findViewById(R.id.progressBar);

        // Get the question from intent
        Intent intent = getIntent();
        String question = intent.getStringExtra("question");

        if (question == null || question.trim().isEmpty()) {
            questionText.setText("No question provided.");
            answerText.setText("Please go back and enter a question.");
            return;
        }

        // Set question text
        questionText.setText(question);

        // Show loading UI
        progressBar.setVisibility(View.VISIBLE);
        answerText.setText("Fetching answer...");

        // Fetch AI-generated answer
        fetchAnswer(question);

        // Back button functionality
        backButton.setOnClickListener(v -> finish());

        // Translate button functionality
        translateButton.setOnClickListener(v -> translateResponse());
    }

    // Fetch AI-generated answer using Gemini API
    private void fetchAnswer(String question) {
        geminiApiHelper.generateResponse(question, new GeminiApiHelper.GeminiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    answerText.setText(response);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    answerText.setText("Error: " + error);
                    Toast.makeText(DairySolutions.this, "Failed to fetch answer", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Translate response text to Hindi
    private void translateResponse() {
        String responseText = answerText.getText().toString();
        if (responseText.isEmpty()) {
            Toast.makeText(this, "No text to translate!", Toast.LENGTH_SHORT).show();
            return;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build();

        Translator translator = Translation.getClient(options);
        translator.downloadModelIfNeeded().addOnSuccessListener(unused -> {
            translator.translate(responseText).addOnSuccessListener(translatedText -> {
                answerText.setText(translatedText);
            }).addOnFailureListener(e -> Toast.makeText(this, "Translation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Model download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        geminiApiHelper.shutdown(); // Shutdown API helper properly
    }
}
