package com.example.client.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Year implements Parcelable {
    private int id;
    private String name;

    public Year(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    protected Year(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static final Creator<Year> CREATOR = new Creator<Year>() {
        @Override
        public Year createFromParcel(Parcel in) {
            return new Year(in);
        }

        @Override
        public Year[] newArray(int size) {
            return new Year[size];
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
    }

    public static Year fromJson(JSONObject json) {
        try {
            return new Year(
                    json.getInt("id"),
                    json.getString("name")
            );
        } catch (JSONException e) {
            return null;
        }
    }
}