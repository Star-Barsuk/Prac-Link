package com.example.client.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Course implements Parcelable {
    private int id;
    private String name;

    public Course(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    protected Course(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
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

    public static Course fromJson(JSONObject json) {
        try {
            return new Course(
                    json.getInt("id"),
                    json.getString("name")
            );
        } catch (JSONException e) {
            return null;
        }
    }
}