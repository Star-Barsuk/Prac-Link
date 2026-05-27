package com.example.client.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Group implements Parcelable {
    private int id;
    private String name;
    private int courseId;

    public Group(int id, String name, int courseId) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCourseId() { return courseId; }

    protected Group(Parcel in) {
        id = in.readInt();
        name = in.readString();
        courseId = in.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
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
        dest.writeInt(courseId);
    }

    public static Group fromJson(JSONObject json) {
        try {
            return new Group(
                    json.getInt("id"),
                    json.getString("name"),
                    json.optInt("course_id")
            );
        } catch (JSONException e) {
            return null;
        }
    }
}