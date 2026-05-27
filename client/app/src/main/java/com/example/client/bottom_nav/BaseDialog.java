package com.example.client.bottom_nav;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

public abstract class BaseDialog<B extends ViewBinding> {
    protected final AlertDialog dialog;
    protected final B binding;

    public BaseDialog(@NonNull Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = inflateBinding(inflater);
        View dialogView = binding != null ? binding.getRoot() : new View(context);

        this.dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
    }

    protected abstract B inflateBinding(LayoutInflater inflater);

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}