package com.example.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.client.databinding.ItemChatBinding;
import com.example.client.models.Chat;

import java.util.List;

public class ChatAdapter extends BaseListAdapter<Chat, ItemChatBinding, ChatAdapter.ChatViewHolder> {

    private OnChatClickListener clickListener;
    private OnChatLongClickListener longClickListener;

    public interface OnChatClickListener {
        void onChatClicked(Chat chat);
    }
    public interface OnChatLongClickListener {
        void onChatLongClicked(Chat chat);
    }

    public ChatAdapter(@NonNull Context context, @NonNull List<Chat> chats) {
        super(context, chats);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.clickListener = listener;
    }
    public void setOnChatLongClickListener(OnChatLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    protected ItemChatBinding createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemChatBinding.inflate(inflater, parent, false);
    }

    @NonNull
    @Override
    protected ChatViewHolder createViewHolder(@NonNull ItemChatBinding binding) {
        return new ChatViewHolder(binding);
    }

    @Override
    protected void bindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class ChatViewHolder extends BaseViewHolder<ItemChatBinding> {
        public ChatViewHolder(@NonNull ItemChatBinding binding) {
            super(binding);
        }

        void bind(@NonNull Chat chat) {
            binding.chatnameTv.setText(chat.getChatName());
            binding.lastMessageTv.setText("Последнее сообщение");
            binding.timestampTv.setText(chat.getCreatedAt());

            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onChatClicked(chat);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onChatLongClicked(chat);
                    return true;
                }
                return false;
            });
        }
    }
}