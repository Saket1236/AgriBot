package com.example.agribot;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiHelper {
    private static final String API_KEY = "AIzaSyD8FooJraEXTs0ERttLlB2OymyeU951dpE";
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface GeminiResponseCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    public void generateResponse(String prompt, GeminiResponseCallback callback) {
        executorService.execute(() -> {
            try {
                // 🔹 Create JSON request payload
                JSONObject textPart = new JSONObject().put("text", prompt);
                JSONArray partsArray = new JSONArray().put(textPart);
                JSONObject contentObject = new JSONObject().put("parts", partsArray);
                JSONArray contentsArray = new JSONArray().put(contentObject);
                JSONObject jsonRequest = new JSONObject().put("contents", contentsArray);

                // 🔹 Build HTTP request
                RequestBody body = RequestBody.create(jsonRequest.toString(), JSON);
                Request request = new Request.Builder()
                        .url(GEMINI_API_URL)
                        .post(body)
                        .build();

                // 🔹 Execute API Call
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("API_RESPONSE", responseData); // Debugging log

                        String aiResponse = parseGeminiResponse(responseData);
                        if (aiResponse != null) {
                            callback.onSuccess(aiResponse);
                        } else {
                            callback.onFailure("AI response format invalid.");
                        }
                    } else {
                        callback.onFailure("API Error: " + response.message());
                    }
                }
            } catch (IOException | JSONException e) {
                callback.onFailure("Request Failed: " + e.getMessage());
            }
        });
    }

    // 🔹 Function to parse Gemini API response correctly
    private String parseGeminiResponse(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray candidates = jsonResponse.optJSONArray("candidates");

            if (candidates != null && candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.optJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.optJSONArray("parts");
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "No valid response from AI.");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", "Error parsing response: " + e.getMessage());
        }
        return null;
    }

    // 🔹 Shutdown executor properly
    public void shutdown() {
        executorService.shutdown();
    }
}
