package com.example.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.client.databinding.ItemMessageIncomingBinding;
import com.example.client.databinding.ItemMessageOutgoingBinding;
import com.example.client.models.Message;
import com.example.client.view_models.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends BaseListAdapter<Message, ItemMessageIncomingBinding, MessageAdapter.MessageViewHolder> {
    private static final int TYPE_OUTGOING = 0;
    private static final int TYPE_INCOMING = 1;

    private final int currentUserId;
    private final SimpleDateFormat dateFormat;

    public MessageAdapter(@NonNull Context context, @NonNull List<Message> messages) {
        super(context, messages);
        this.currentUserId = UserViewModel.getInstance().getCurrentUser().getValue().getId();
        this.dateFormat = new SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault());
    }

    public void addMessage(Message message) {
        this.items.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getSender().getId() == currentUserId
                ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @NonNull
    @Override
    protected ItemMessageIncomingBinding createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemMessageIncomingBinding.inflate(inflater, parent, false);
    }

    @NonNull
    @Override
    protected MessageViewHolder createViewHolder(@NonNull ItemMessageIncomingBinding binding) {
        return new MessageViewHolder(binding);
    }

    @Override
    protected void bindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = getItem(position);
        boolean isOutgoing = getItemViewType(position) == TYPE_OUTGOING;
        holder.bind(message, isOutgoing, dateFormat);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        int type = getItemViewType(position);
        MessageViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            if (type == TYPE_OUTGOING) {
                ItemMessageOutgoingBinding outgoingBinding =
                        ItemMessageOutgoingBinding.inflate(inflater, parent, false);
                holder = new MessageViewHolder(outgoingBinding);
                convertView = outgoingBinding.getRoot();
            } else {
                ItemMessageIncomingBinding incomingBinding =
                        ItemMessageIncomingBinding.inflate(inflater, parent, false);
                holder = new MessageViewHolder(incomingBinding);
                convertView = incomingBinding.getRoot();
            }

            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        Message message = getItem(position);
        holder.bind(message, type == TYPE_OUTGOING, dateFormat);
        return convertView;
    }

    protected static class MessageViewHolder extends BaseViewHolder<ItemMessageIncomingBinding> {
        private Object binding;

        public MessageViewHolder(@NonNull ItemMessageIncomingBinding binding) {
            super(binding);
            this.binding = binding;
        }

        public MessageViewHolder(@NonNull ItemMessageOutgoingBinding binding) {
            super(null);
            this.binding = binding;
        }

        void bind(Message message, boolean isOutgoing, SimpleDateFormat dateFormat) {
            String formattedDate = dateFormat.format(message.getSentAt());

            if (isOutgoing) {
                ItemMessageOutgoingBinding outgoingBinding = (ItemMessageOutgoingBinding) binding;
                outgoingBinding.messageTv.setText(message.getContent());
                outgoingBinding.messageDateTv.setText(formattedDate);
            } else {
                ItemMessageIncomingBinding incomingBinding = (ItemMessageIncomingBinding) binding;
                incomingBinding.messageTv.setText(message.getContent());
                incomingBinding.messageDateTv.setText(formattedDate);
                incomingBinding.usernameTv.setText(message.getSender().getUsername());
            }
        }
    }
}