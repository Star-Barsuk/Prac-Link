package com.example.client.bottom_nav.chats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.client.ChatActivity;
import com.example.client.adapters.ChatAdapter;
import com.example.client.bottom_nav.BaseFragment;
import com.example.client.databinding.FragmentChatsBinding;
import com.example.client.models.Chat;
import com.example.client.models.User;
import com.example.client.network.ChatRequests;
import com.example.client.view_models.UserViewModel;

import java.util.ArrayList;

public class ChatsFragment extends BaseFragment<FragmentChatsBinding> {
    private ChatAdapter chatAdapter;

    @Override
    protected FragmentChatsBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentChatsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        loadChats();
    }

    private void setupAdapter() {
        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>());
        chatAdapter.setOnChatClickListener(this::openChatActivity);
        chatAdapter.setOnChatLongClickListener(this::showDeleteChatDialog);
        binding.chatsLv.setAdapter(chatAdapter);
    }

    private void openChatActivity(Chat chat) {
        if (chat == null) {
            Toast.makeText(requireContext(), "Некорректные данные чата", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireActivity(), ChatActivity.class);
        intent.putExtra("chat_id", chat.getId());

        if (chat.getOtherUser() != null) {
            intent.putExtra("other_user", chat.getOtherUser());
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e("ChatsFragment", "Error opening chat with id: " + chat.getId(), e);
            Toast.makeText(requireContext(), "Ошибка при открытии чата", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChats() {
        setLoadingState(true);
        User currentUser = userViewModel.getCurrentUser().getValue();
        if (currentUser == null) {
            setLoadingState(false);
            return;
        }

        ChatRequests.getUserChats(requireContext(), currentUser.getId(),
                (success, message, chats) -> {
                    setLoadingState(false);
                    if (success) {
                        if (chats == null || chats.isEmpty()) {
                            binding.emptyStateTv.setVisibility(View.VISIBLE);
                            binding.chatsLv.setVisibility(View.GONE);
                        } else {
                            binding.emptyStateTv.setVisibility(View.GONE);
                            binding.chatsLv.setVisibility(View.VISIBLE);
                            chatAdapter.updateList(chats);
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDeleteChatDialog(Chat chat) {
        new ChatActionDialog.Builder(requireContext())
                .setOnConfirmAction(() -> deleteChatWithUser(chat))
                .setOnCancelAction(() -> { })
                .setOnDeleteChatAction(() -> { })
                .build()
                .show();
    }

    private void deleteChatWithUser(Chat chat) {
        User currentUser = UserViewModel.getInstance().getCurrentUser().getValue();
        if (currentUser == null || chat == null) return;

        setLoadingState(true);
        ChatRequests.deleteChat(requireContext(),
                chat.getId(),
                (success, message) -> {
                    setLoadingState(false);
                    if (success) {
                        loadChats();
                        Toast.makeText(requireContext(),
                                "Чат удалён", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Ошибка при удалении чата: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}