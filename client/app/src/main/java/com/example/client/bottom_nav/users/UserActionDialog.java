package com.example.client.bottom_nav.users;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.example.client.bottom_nav.BaseDialog;
import com.example.client.databinding.DialogUserActionBinding;

public class UserActionDialog extends BaseDialog<DialogUserActionBinding> {
    private Runnable onConfirmAction;
    private Runnable onCancelAction;
    private Runnable onCreateChatAction;

    public UserActionDialog(@NonNull Context context) {
        super(context);
        setupListeners();
    }

    @Override
    protected DialogUserActionBinding inflateBinding(LayoutInflater inflater) {
        return DialogUserActionBinding.inflate(inflater);
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

        binding.createChatTv.setOnClickListener(v -> {
            if (onCreateChatAction != null) onCreateChatAction.run();
        });
    }

    public UserActionDialog setOnConfirmAction(Runnable action) {
        this.onConfirmAction = action;
        return this;
    }

    public UserActionDialog setOnCancelAction(Runnable action) {
        this.onCancelAction = action;
        return this;
    }

    public UserActionDialog setOnCreateChatAction(Runnable action) {
        this.onCreateChatAction = action;
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public static class Builder {
        private final UserActionDialog dialog;

        public Builder(Context context) {
            dialog = new UserActionDialog(context);
        }

        public Builder setOnConfirmAction(Runnable action) {
            dialog.setOnConfirmAction(action);
            return this;
        }

        public Builder setOnCancelAction(Runnable action) {
            dialog.setOnCancelAction(action);
            return this;
        }

        public Builder setOnCreateChatAction(Runnable action) {
            dialog.setOnCreateChatAction(action);
            return this;
        }

        public UserActionDialog build() {
            return dialog;
        }
    }
}