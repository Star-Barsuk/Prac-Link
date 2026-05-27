package com.example.client.network;

import android.content.Context;
import android.util.Log;

import com.example.client.models.Message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageRequests extends ApiRequest {
    private static final String TAG = MessageRequests.class.getSimpleName();

    public interface MessagesResponseCallback {
        void onResponse(boolean success, String message, List<Message> messages);
    }

    public interface MessageResponseCallback {
        void onResponse(boolean success, String message, Message messageObj);
    }

    public static void getChatMessages(Context context, int chatId, MessagesResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/messages/" + chatId;
        try {
            sendRequest(context, url, null, "GET",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        try {
                            if (!success) {
                                callback.onResponse(false, message, null);
                                return;
                            }

                            List<Message> messages = Message.listFromJson(response);
                            callback.onResponse(true, message, messages);
                        } catch (Exception e) {
                            Log.e(TAG, "Parsing error", e);
                            callback.onResponse(false, "Failed to parse messages", null);
                        }
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Request error", e);
            callback.onResponse(false, "Network error", null);
        }
    }

    public static void getNewMessages(Context context, int chatId, int lastMessageId, MessagesResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/messages/" + chatId + "/new?last_id=" + lastMessageId;
        try {
            sendRequest(context, url, null, "GET",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        if (!success) {
                            if (message != null && message.contains("404")) {
                                callback.onResponse(true, "No new messages", new ArrayList<>());
                            } else {
                                callback.onResponse(false, message, null);
                            }
                            return;
                        }

                        try {
                             List<Message> messages = Message.listFromJson(response);
                            callback.onResponse(true, message, messages);
                        } catch (Exception e) {
                            Log.e(TAG, "Parsing error", e);
                            callback.onResponse(false, "Failed to parse new messages", null);
                        }
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Request error", e);
            callback.onResponse(false, "Network error", null);
        }
    }

    public static void sendMessage(Context context, int chatId, int senderId, String content,
                                   MessageResponseCallback callback) {
        String url = ServerConfig.BASE_URL + "/messages";
        try {
            JSONObject json = new JSONObject();
            json.put("chat_id", chatId);
            json.put("sender_id", senderId);
            json.put("content", content);

            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) -> runOnUiThread(context, () -> {
                        try {
                            if (!success) {
                                callback.onResponse(false, message, null);
                                return;
                            }

                            Message newMessage = Message.fromJson(response);
                            callback.onResponse(true, message, newMessage);
                        } catch (Exception e) {
                            Log.e(TAG, "Parsing error", e);
                            callback.onResponse(false, "Failed to parse response", null);
                        }
                    }));
        } catch (Exception e) {
            Log.e(TAG, "Request error", e);
            callback.onResponse(false, "Network error", null);
        }
    }
}