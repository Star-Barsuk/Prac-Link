package com.example.client.network;

import android.content.Context;
import android.util.Log;

import com.example.client.models.User;

import java.util.List;

public class UserRequests extends ApiRequest {
    private static final String TAG = UserRequests.class.getSimpleName();

    public interface UsersResponseCallback {
        void onResponse(boolean success, String message, List<User> users);
    }

    public static void getAllUsers(Context context, UsersResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/users";
        try {
            sendRequest(context, url, null, "GET",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        List<User> users = success ? User.listFromJson(response) : null;
                        callback.onResponse(success, message, users);

                    }));
        } catch (Exception e) {
            Log.e(TAG, "Get users error", e);
            callback.onResponse(false, "Get users request creation failed.", null);
        }
    }

    public static void getAllUsersExclude(Context context, int excludeUserId, UsersResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/users/exclude/" + excludeUserId;
        try {
            sendRequest(context, url, null, "GET",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        List<User> users = success ? User.listFromJson(response) : null;
                        callback.onResponse(success, message, users);
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Get users error", e);
            callback.onResponse(false, "Get users request failed.", null);
        }
    }
}