package com.example.client.bottom_nav.chats;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.example.client.bottom_nav.BaseDialog;
import com.example.client.databinding.DialogChatActionBinding;

public class ChatActionDialog extends BaseDialog<DialogChatActionBinding> {
    private Runnable onConfirmAction;
    private Runnable onCancelAction;
    private Runnable onDeleteChatAction;

    public ChatActionDialog(@NonNull Context context) {
        super(context);
        setupListeners();
    }

    @Override
    protected DialogChatActionBinding inflateBinding(LayoutInflater inflater) {
        return DialogChatActionBinding.inflate(inflater);
    }

    private void setupListeners() {
        binding.cancelBtn.setOnClickListener(v -> {
            if (onCancelAction != null) onCancelAction.run();
            dismiss();
        });

        binding.confirmBtn.setOnClickListener(v -> {
            if (onConfirmAction != null) onConfirmAction.run();
            dismiss();
        });

        binding.deleteChatTv.setOnClickListener(v -> {
            if (onDeleteChatAction != null) onDeleteChatAction.run();
        });
    }

    public ChatActionDialog setOnConfirmAction(Runnable action) {
        this.onConfirmAction = action;
        return this;
    }

    public ChatActionDialog setOnCancelAction(Runnable action) {
        this.onCancelAction = action;
        return this;
    }

    public ChatActionDialog setOnDeleteAction(Runnable action) {
        this.onDeleteChatAction = action;
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public static class Builder {
        private final ChatActionDialog dialog;

        public Builder(Context context) {
            dialog = new ChatActionDialog(context);
        }

        public Builder setOnConfirmAction(Runnable action) {
            dialog.setOnConfirmAction(action);
            return this;
        }

        public Builder setOnCancelAction(Runnable action) {
            dialog.setOnCancelAction(action);
            return this;
        }

        public Builder setOnDeleteChatAction(Runnable action) {
            dialog.setOnDeleteAction(action);
            return this;
        }

        public ChatActionDialog build() {
            return dialog;
        }
    }
}