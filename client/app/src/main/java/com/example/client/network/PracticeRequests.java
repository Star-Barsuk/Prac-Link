package com.example.client.network;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.client.models.Course;
import com.example.client.models.Group;
import com.example.client.models.PracticeBase;
import com.example.client.models.Year;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PracticeRequests extends ApiRequest {
    private static final String TAG = PracticeRequests.class.getSimpleName();

    public interface YearsCallback {
        void onResponse(boolean success, String message, List<Year> years);
    }

    public interface CoursesCallback {
        void onResponse(boolean success, String message, List<Course> courses);
    }

    public interface GroupsCallback {
        void onResponse(boolean success, String message, List<Group> groups);
    }

    public interface PracticeBasesCallback {
        void onResponse(boolean success, String message, List<PracticeBase> bases);
    }

    public interface PracticeRegistrationCallback {
        void onResponse(boolean success, String message);
    }

    public static void getYears(Context context, YearsCallback callback) {
        String url = ServerConfig.BASE_URL + "/years";
        sendRequest(context, url, null, "GET",
                (success, message, response) -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray yearsArray = json.getJSONArray("years");

                            List<Year> years = new ArrayList<>();
                            for (int i = 0; i < yearsArray.length(); i++) {
                                Year year = Year.fromJson(yearsArray.getJSONObject(i));
                                if (year != null) {
                                    years.add(year);
                                }
                            }
                            callback.onResponse(true, message, years);
                        } catch (JSONException e) {
                            callback.onResponse(false, "Error parsing response", null);
                        }
                    } else {
                        callback.onResponse(false, message, null);
                    }
                });
    }

    public static void getCourses(Context context, int yearId, CoursesCallback callback) {
        String url = ServerConfig.BASE_URL + "/courses?year_id=" + yearId;
        sendRequest(context, url, null, "GET",
                (success, message, response) -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray coursesArray = json.getJSONArray("courses");

                            List<Course> courses = new ArrayList<>();
                            for (int i = 0; i < coursesArray.length(); i++) {
                                Course course = Course.fromJson(coursesArray.getJSONObject(i));
                                if (course != null) {
                                    courses.add(course);
                                }
                            }
                            callback.onResponse(true, message, courses);
                        } catch (JSONException e) {
                            callback.onResponse(false, "Error parsing response", null);
                        }
                    } else {
                        callback.onResponse(false, message, null);
                    }
                });
    }

    public static void getGroups(Context context, int yearId, int courseId, GroupsCallback callback) {
        String url = ServerConfig.BASE_URL + "/groups?year_id=" + yearId + "&course_id=" + courseId;
        sendRequest(context, url, null, "GET",
                (success, message, response) -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray groupsArray = json.getJSONArray("groups");

                            List<Group> groups = new ArrayList<>();
                            for (int i = 0; i < groupsArray.length(); i++) {
                                Group group = Group.fromJson(groupsArray.getJSONObject(i));
                                if (group != null) {
                                    groups.add(group);
                                }
                            }
                            callback.onResponse(true, message, groups);
                        } catch (JSONException e) {
                            callback.onResponse(false, "Error parsing response", null);
                        }
                    } else {
                        callback.onResponse(false, message, null);
                    }
                });
    }

    public static void getAvailablePracticeBases(Context context,
                                                 int yearId,
                                                 int courseId,
                                                 int groupId,
                                                 PracticeBasesCallback callback) {
        String url = ServerConfig.BASE_URL + "/practice/bases?year_id=" + yearId + "&course_id=" + courseId;

        if (groupId > 0) {
            url += "&group_id=" + groupId;
        }

        sendRequest(context, url, null, "GET",
                (success, message, response) -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray basesArray = json.getJSONArray("bases");

                            List<PracticeBase> bases = new ArrayList<>();
                            for (int i = 0; i < basesArray.length(); i++) {
                                PracticeBase base = PracticeBase.fromJson(basesArray.getJSONObject(i));
                                if (base != null) {
                                    bases.add(base);
                                }
                            }
                            callback.onResponse(true, message, bases);
                        } catch (JSONException e) {
                            callback.onResponse(false, message, null);
                        }
                    } else {
                        callback.onResponse(false, message, null);
                    }
                });
    }

    public static void getUserPracticeBases(Context context,
                                            int userId,
                                            PracticeBasesCallback callback) {
        String url = ServerConfig.BASE_URL + "/practice/user/" + userId;
        sendRequest(context, url, null, "GET",
                (success, message, response) -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray basesArray = json.getJSONArray("practice_bases");

                            List<PracticeBase> bases = new ArrayList<>();
                            for (int i = 0; i < basesArray.length(); i++) {
                                PracticeBase base = PracticeBase.fromJson(basesArray.getJSONObject(i));
                                if (base != null) {
                                    bases.add(base);
                                }
                            }
                            callback.onResponse(true, message, bases);
                        } catch (JSONException e) {
                            callback.onResponse(false, "Error parsing response", null);
                        }
                    } else {
                        callback.onResponse(false, message, null);
                    }
                });
    }

    public static void registerForPractice(Context context,
                                           int userId,
                                           int baseId,
                                           PracticeRegistrationCallback callback) {
        String url = ServerConfig.BASE_URL + "/practice/register";
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("base_id", baseId);

            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) ->
                            callback.onResponse(success, message));
        } catch (JSONException e) {
            callback.onResponse(false, "Error creating request");
        }
    }

    public static void unregisterFromPractice(Context context,
                                           int userId,
                                           int baseId,
                                           PracticeRegistrationCallback callback) {
        String url = ServerConfig.BASE_URL + "/practice/unregister";
        try {
            JSONObject json = new JSONObject();
            json.put("user_id", userId);
            json.put("base_id", baseId);

            sendRequest(context, url, json.toString(), "POST",
                    (success, message, response) ->
                            callback.onResponse(success, message));
        } catch (JSONException e) {
            callback.onResponse(false, "Error creating request");
        }
    }
}