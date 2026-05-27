package com.example.client.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class ApiRequest {
    private static final String TAG = ApiRequest.class.getSimpleName();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public interface ResponseCallback {
        void onResponse(boolean success, String message, String response);
    }

    protected static void sendRequest(Context context,
                                      String url,
                                      String json,
                                      String method,
                                      ResponseCallback callback) {

        RequestBody body = json != null ? RequestBody.create(json, JSON_MEDIA_TYPE) : null;
        Request request = buildRequest(url, method, body);

        Log.i(TAG, "→ REQUEST: " + method + " " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Request failed: " + url, e);
                handleError(context, "Нет соединения с сервером", callback);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.i(TAG, "← RESPONSE: " + url + " | Code: " + response.code());

                if (response.isSuccessful()) {
                    handleSuccessResponse(context, responseBody, callback);
                } else {
                    handleErrorResponse(context, response.code(), responseBody, url, callback);
                }
            }
        });
    }

    private static Request buildRequest(String url, String method, RequestBody body) {
        Request.Builder builder = new Request.Builder().url(url);
        switch (method.toUpperCase()) {
            case "POST": return body != null ? builder.post(body).build() : builder.post(RequestBody.create("", JSON_MEDIA_TYPE)).build();
            case "PUT":  return body != null ? builder.put(body).build() : builder.put(RequestBody.create("", JSON_MEDIA_TYPE)).build();
            case "DELETE": return body != null ? builder.delete(body).build() : builder.delete().build();
            default: return builder.get().build();
        }
    }

    private static void handleSuccessResponse(Context context, String responseBody, ResponseCallback callback) {
        String message = "Success";
        try {
            if (responseBody.startsWith("{")) {
                JSONObject json = new JSONObject(responseBody);
                if (json.has("message")) message = json.getString("message");
            }
        } catch (Exception ignored) {}
        callback.onResponse(true, message, responseBody);
    }

    private static void handleErrorResponse(Context context, int statusCode, String responseBody, String url, ResponseCallback callback) {
        String errorMessage = "Error occurred";

        try {
            if (responseBody.startsWith("{")) {
                JSONObject json = new JSONObject(responseBody);
                if (json.has("detail")) errorMessage = json.getString("detail");
                else if (json.has("message")) errorMessage = json.getString("message");
            }
        } catch (Exception ignored) {}

        if (url.contains("/new") || url.contains("/messages/")) {
            callback.onResponse(false, errorMessage, null);
            return;
        }

        if (statusCode == 404) {
            callback.onResponse(false, errorMessage, null);
            return;
        }

        handleError(context, errorMessage, callback);
    }

    private static void handleError(Context context, String message, ResponseCallback callback) {
        if (!message.contains("not found") &&
                !message.contains("No new") &&
                !message.contains("Error occurred")) {

            showToast(context, message);
        }
        callback.onResponse(false, message, null);
    }

    private static void showToast(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (context != null && context.getApplicationContext() != null) {
                Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected static void runOnUiThread(Context context, Runnable action) {
        if (context instanceof android.app.Activity && !((android.app.Activity) context).isFinishing()) {
            ((android.app.Activity) context).runOnUiThread(action);
        } else {
            new Handler(Looper.getMainLooper()).post(action);
        }
    }
}