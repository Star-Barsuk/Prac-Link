package com.example.client.network;

import android.content.Context;
import android.util.Log;

import com.example.client.models.User;

import org.json.JSONObject;

public class AuthRequests extends ApiRequest {
    private static final String TAG = AuthRequests.class.getSimpleName();

    public interface AuthResponseCallback {
        void onResponse(boolean success, String message, User user);
    }

    public static void login(Context context,
                             String email,
                             String password,
                             AuthResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/users/login";
        try {
            JSONObject json = User.toJson(email, password, null);
            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        User user = success ? User.fromJson(response) : null;
                        callback.onResponse(success, message, user);
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            callback.onResponse(false, "Login request creation failed.", null);
        }
    }

    public static void register(Context context,
                                String username,
                                String email,
                                String password,
                                AuthResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/users/register";
        try {
            JSONObject json = User.toJson(email, password, username);
            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        User user = success ? User.fromJson(response) : null;
                        callback.onResponse(success, message, user);
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Register error", e);
            callback.onResponse(false, "Registration request creation failed.", null);
        }
    }
}