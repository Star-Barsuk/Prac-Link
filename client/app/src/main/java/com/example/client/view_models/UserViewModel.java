package com.example.client.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.client.models.User;

public class UserViewModel {
    private static UserViewModel instance;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    private UserViewModel() {}
    public static UserViewModel getInstance() {
        if (instance == null) {
            instance = new UserViewModel();
        }
        return instance;
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { currentUser.postValue(user); }

    public void logout() {
        currentUser.postValue(null);
    }
}