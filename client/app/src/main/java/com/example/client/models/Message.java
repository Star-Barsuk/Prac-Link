package com.example.client.models;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Message {
    private static final String TAG = Message.class.getSimpleName();
    @SuppressLint("ConstantLocale")
    private static final List<SimpleDateFormat> DATE_FORMATS = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    );

    private int id;
    private int chatId;
    private User sender;
    private String content;
    private Date sentAt;

    public Message(int id, int chatId, User sender, String content, Date sentAt) {
        this.id = id;
        this.chatId = chatId;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getId() { return id; }
    public int getChatId() { return chatId; }
    public User getSender() { return sender; }
    public String getContent() { return content; }
    public Date getSentAt() { return sentAt; }

    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return new Date();
        }

        dateString = dateString.replace("\"", "").trim();

        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                return format.parse(dateString);
            } catch (ParseException e) {
            }
        }

        Log.e(TAG, "Failed to parse date: " + dateString);
        return new Date();
    }

    public static Message fromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            return fromJson(json);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message data", e);
            return null;
        }
    }

    public static Message fromJson(JSONObject json) {
        try {
            JSONObject senderJson = json.getJSONObject("sender");
            User sender = new User(
                    senderJson.getInt("id"),
                    senderJson.getString("username"),
                    senderJson.optString("email", "")
            );

            Date sentAt = parseDate(json.getString("sent_at"));

            return new Message(
                    json.getInt("id"),
                    json.optInt("chat_id", -1),
                    sender,
                    json.getString("content"),
                    sentAt
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message", e);
            return null;
        }
    }

    public static List<Message> listFromJson(String jsonString) {
        List<Message> messages = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray messagesArray = root.getJSONArray("messages");

            for (int i = 0; i < messagesArray.length(); i++) {
                Message message = fromJson(messagesArray.getJSONObject(i));
                if (message != null) {
                    messages.add(message);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing messages list", e);
        }
        return messages;
    }
}