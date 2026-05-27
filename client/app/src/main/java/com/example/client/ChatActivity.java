package com.example.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.adapters.MessageAdapter;
import com.example.client.databinding.ActivityChatBinding;
import com.example.client.models.Message;
import com.example.client.models.User;
import com.example.client.network.MessageRequests;
import com.example.client.view_models.UserViewModel;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;

    private int chatId;
    private User otherUser;

    private int lastMessageId = 0;
    private Handler refreshHandler;
    private static final long REFRESH_DELAY = 2500;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadNewMessages();
            if (refreshHandler != null) {
                refreshHandler.postDelayed(this, REFRESH_DELAY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding.getRoot().setFitsSystemWindows(true);

        refreshHandler = new Handler(Looper.getMainLooper());

        chatId = getIntent().getIntExtra("chat_id", -1);
        otherUser = getIntent().getParcelableExtra("other_user");

        if (chatId == -1) {
            finish();
            return;
        }

        if (otherUser != null) {
            setTitle(otherUser.getUsername());
        } else {
            setTitle("General Chat");
        }

        setupAdapter();
        setupListeners();
        loadInitialMessages();
    }

    private void setupAdapter() {
        messageAdapter = new MessageAdapter(this, new ArrayList<>());
        binding.messagesLv.setAdapter(messageAdapter);
    }

    private void loadInitialMessages() {
        MessageRequests.getChatMessages(this, chatId, (success, message, messages) -> {
            if (success && messages != null) {
                messageAdapter.updateList(messages);
                if (!messages.isEmpty()) {
                    lastMessageId = messages.get(messages.size() - 1).getId();
                }
                scrollToBottom();
            }
        });
    }

    private void loadNewMessages() {
        MessageRequests.getNewMessages(this, chatId, lastMessageId,
                (success, message, messages) -> {
                    if (success && messages != null && !messages.isEmpty()) {
                        for (Message msg : messages) {
                            messageAdapter.addMessage(msg);
                        }
                        lastMessageId = messages.get(messages.size() - 1).getId();
                        scrollToBottom();
                    }
                });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.sendMessageBtn.setOnClickListener(v -> sendMessage());

        binding.messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.sendMessageBtn.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void sendMessage() {
        String content = binding.messageEt.getText().toString().trim();
        if (content.isEmpty()) return;

        User currentUser = UserViewModel.getInstance().getCurrentUser().getValue();
        if (currentUser == null) return;

        binding.messageEt.setText("");
        binding.sendMessageBtn.setEnabled(false);

        MessageRequests.sendMessage(this, chatId, currentUser.getId(), content,
                (success, message, newMessage) -> {
                    binding.sendMessageBtn.setEnabled(true);
                    if (success && newMessage != null) {
                        messageAdapter.addMessage(newMessage);
                        lastMessageId = Math.max(lastMessageId, newMessage.getId());
                        scrollToBottom();
                    }
                });
    }

    private void scrollToBottom() {
        binding.messagesLv.post(() -> {
            if (messageAdapter.getCount() > 0) {
                binding.messagesLv.setSelection(messageAdapter.getCount() - 1);
            }
        });
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshHandler = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRefresh();
    }

    @Override
    public void finish() {
        stopAutoRefresh();
        super.finish();
    }
}