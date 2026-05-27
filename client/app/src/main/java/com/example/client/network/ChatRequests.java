package com.example.client.network;

import android.content.Context;
import android.util.Log;

import com.example.client.models.Chat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ChatRequests extends ApiRequest {
    private static final String TAG = ChatRequests.class.getSimpleName();

    public interface ChatsResponseCallback {
        void onResponse(boolean success, String message, List<Chat> chats);
    }

    public interface ChatResponseCallback {
        void onResponse(boolean success, String message, Chat chat);
    }

    public interface BasicResponseCallback {
        void onResponse(boolean success, String message);
    }

    public static void getUserChats(Context context, int userId, ChatsResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/chats?user_id=" + userId;
        sendRequest(context, url, null, "GET",
                (success, message, response) -> runOnUiThread(context, () -> {
                    List<Chat> chats = success ? Chat.listFromJson(response, userId) : null;
                    callback.onResponse(success, message, chats);
                }));
    }

    public static void startChat(Context context, String chatName, List<String> userIds, int currentUserId, ChatResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/chats";
        try {
            JSONObject json = new JSONObject();
            json.put("name", chatName);
            json.put("user_ids", new JSONArray(userIds));

            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        Chat chat = success ? Chat.fromJson(response, currentUserId) : null;
                        callback.onResponse(success, message, chat);
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Start chat error", e);
            callback.onResponse(false, "Start chat request failed.", null);
        }
    }

    public static void deleteChat(Context context, int chatId, BasicResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/chats/" + chatId;
        sendRequest(context, url, null, "DELETE",
                (success, message, response) -> runOnUiThread(context, () -> {
                    callback.onResponse(success, message);
                }));
    }
}