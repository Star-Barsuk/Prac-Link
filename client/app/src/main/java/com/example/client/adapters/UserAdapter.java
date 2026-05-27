package com.example.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.client.R;
import com.example.client.databinding.ItemUserBinding;
import com.example.client.models.User;

import java.util.List;

public class UserAdapter extends BaseListAdapter<
        User,
        ItemUserBinding,
        UserAdapter.UserViewHolder> {

    private OnUserLongClickListener longClickListener;

    public interface OnUserLongClickListener {
        void onUserLongClicked(User user);
    }

    public UserAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, users);
    }

    public void setOnUserLongClickListener(OnUserLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    protected ItemUserBinding createBinding(@NonNull LayoutInflater inflater,
                                            @NonNull ViewGroup parent) {
        return ItemUserBinding.inflate(inflater, parent, false);
    }

    @NonNull
    @Override
    protected UserViewHolder createViewHolder(@NonNull ItemUserBinding binding) {
        return new UserViewHolder(binding);
    }

    @Override
    protected void bindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class UserViewHolder extends BaseViewHolder<ItemUserBinding> {
        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding);
        }

        void bind(@NonNull User user) {
            binding.usernameTv.setText(user.getUsername());
            binding.profileIv.setImageResource(R.drawable.baseline_person_outline_24);

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onUserLongClicked(user);
                    return true;
                }
                return false;
            });
        }
    }
}