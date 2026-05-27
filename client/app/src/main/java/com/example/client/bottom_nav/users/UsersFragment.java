package com.example.client.bottom_nav.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.client.bottom_nav.BaseFragment;
import com.example.client.adapters.UserAdapter;
import com.example.client.databinding.FragmentUsersBinding;
import com.example.client.models.User;
import com.example.client.network.ChatRequests;
import com.example.client.network.UserRequests;
import com.example.client.view_models.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends BaseFragment<FragmentUsersBinding> {
    private UserAdapter userAdapter;

    @Override
    protected FragmentUsersBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentUsersBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        loadUsers();
    }

    private void setupAdapter() {
        userAdapter = new UserAdapter(requireContext(), new ArrayList<>());
        userAdapter.setOnUserLongClickListener(this::showUserActionsDialog);
        binding.usersLv.setAdapter(userAdapter);
    }

    private void loadUsers() {
        setLoadingState(true);
        UserRequests.getAllUsersExclude(requireContext(),
                userViewModel.getCurrentUser().getValue().getId(),
                (success, message, users) -> {
                    setLoadingState(false);
                    if (success && users != null) {
                        userAdapter.updateList(users);
                    } else {
                        Toast.makeText(requireContext(), "Ошибка: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showUserActionsDialog(User user) {
        new UserActionDialog.Builder(requireContext())
                .setOnConfirmAction(() -> createNewChatWithUser(user))
                .setOnCancelAction(() -> {  })
                .setOnCreateChatAction(() -> {  })
                .build()
                .show();
    }

    private void createNewChatWithUser(User otherUser) {
        User currentUser = UserViewModel.getInstance().getCurrentUser().getValue();
        if (currentUser == null || otherUser == null) return;

        setLoadingState(true);

        String chatName = currentUser.getUsername() + ", " + otherUser.getUsername();
        List<String> userIds = new ArrayList<>();
        userIds.add(String.valueOf(currentUser.getId()));
        userIds.add(String.valueOf(otherUser.getId()));

        ChatRequests.startChat(requireContext(),
                chatName,
                userIds,
                currentUser.getId(),
                (success, message, chat) -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(requireContext(),
                                "Чат с " + otherUser.getUsername() + " создан",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка при создании чата: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}