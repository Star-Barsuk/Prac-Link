package com.example.client.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PracticeBase implements Parcelable {
    private static final String TAG = User.class.getSimpleName();

    private int id;
    private String name;
    private String description;
    private int capacity;
    private int participantsCount;
    private String supervisorName;
    private int supervisorId;

    private List<User> participants;

    public PracticeBase(int id, String name, String description,
                        int capacity, int participantsCount,
                        String supervisorName, int supervisorId,
                        List<User> participants) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.participantsCount = participantsCount;
        this.supervisorName = supervisorName;
        this.supervisorId = supervisorId;
        this.participants = participants;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public int getParticipantsCount() { return participantsCount; }
    public String getSupervisorName() { return supervisorName; }
    public List<User> getParticipants() { return participants; }

    protected PracticeBase(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        capacity = in.readInt();
        participantsCount = in.readInt();
        supervisorName = in.readString();
    }

    public static final Creator<PracticeBase> CREATOR = new Creator<>() {
        @Override
        public PracticeBase createFromParcel(Parcel in) {
            return new PracticeBase(in);
        }

        @Override
        public PracticeBase[] newArray(int size) {
            return new PracticeBase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(capacity);
        dest.writeInt(participantsCount);
        dest.writeString(supervisorName);
    }

    public static PracticeBase fromJson(JSONObject json) {
        try {
            List<User> participants = new ArrayList<>();
            if (json.has("participants")) {
                JSONArray participantsArray = json.getJSONArray("participants");
                for (int i = 0; i < participantsArray.length(); i++) {
                    participants.add(User.fromJson(participantsArray.getJSONObject(i)));
                }
            }

            return new PracticeBase(
                    json.getInt("id"),
                    json.getString("name"),
                    json.getString("description"),
                    json.getInt("capacity"),
                    json.getInt("participants_count"),
                    json.getString("supervisor"),
                    json.getInt("supervisor_id"),
                    participants
            );
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing practice base json", e);
            return null;
        }
    }
}