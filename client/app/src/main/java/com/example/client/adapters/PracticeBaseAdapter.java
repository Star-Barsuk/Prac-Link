package com.example.client.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.client.models.User;
import com.example.client.R;
import com.example.client.databinding.ItemPracticeBinding;
import com.example.client.models.PracticeBase;

import java.util.List;
import java.util.Locale;

public class PracticeBaseAdapter extends BaseListAdapter<
        PracticeBase,
        ItemPracticeBinding,
        PracticeBaseAdapter.PracticeBaseViewHolder> {

    private OnRegisterClickListener registerClickListener;
    private OnUnregisterClickListener unregisterClickListener;
    private int currentUserId;

    public interface OnRegisterClickListener {
        void onRegisterClick(PracticeBase practiceBase);
    }

    public interface OnUnregisterClickListener {
        void onUnregisterClick(PracticeBase practiceBase);
    }

    public PracticeBaseAdapter(@NonNull Context context, @NonNull List<PracticeBase> bases, int currentUserId) {
        super(context, bases);
        this.currentUserId = currentUserId;
    }

    public void setOnRegisterClickListener(OnRegisterClickListener listener) {
        this.registerClickListener = listener;
    }

    public void setOnUnregisterClickListener(OnUnregisterClickListener listener) {
        this.unregisterClickListener = listener;
    }

    @NonNull
    @Override
    protected ItemPracticeBinding createBinding(@NonNull LayoutInflater inflater,
                                                @NonNull ViewGroup parent) {
        return ItemPracticeBinding.inflate(inflater, parent, false);
    }

    @NonNull
    @Override
    protected PracticeBaseViewHolder createViewHolder(@NonNull ItemPracticeBinding binding) {
        return new PracticeBaseViewHolder(binding);
    }

    @Override
    protected void bindViewHolder(@NonNull PracticeBaseViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    protected class PracticeBaseViewHolder extends BaseViewHolder<ItemPracticeBinding> {
        private boolean isExpanded = false;
        private boolean isParticipantsExpanded = false;
        private View expandedView;
        private LinearLayout participantsListContainer;
        private ImageView participantsExpandIcon;

        public PracticeBaseViewHolder(@NonNull ItemPracticeBinding binding) {
            super(binding);
            setupExpandedView(binding.getRoot());

            binding.expandIcon.setOnClickListener(v -> toggleExpand());
            binding.getRoot().setOnClickListener(null);
        }

        private void setupExpandedView(View root) {
            LayoutInflater inflater = LayoutInflater.from(root.getContext());

            expandedView = inflater.inflate(R.layout.item_practice_expanded, (ViewGroup) root, false);
            ((ViewGroup) root).addView(expandedView);

            expandedView.setVisibility(View.GONE);
            participantsListContainer = expandedView.findViewById(R.id.participants_list_container);
            participantsExpandIcon = expandedView.findViewById(R.id.participants_expand_icon);

            participantsExpandIcon.setOnClickListener(v -> toggleParticipantsExpand());
        }

        @SuppressLint("SetTextI18n")
        void bind(@NonNull PracticeBase base) {
            binding.baseName.setText(base.getName());

            if (expandedView != null) {
                TextView description = expandedView.findViewById(R.id.base_description);
                TextView supervisor = expandedView.findViewById(R.id.supervisor_name);
                TextView participants = expandedView.findViewById(R.id.participants_info);
                Button registerButton = expandedView.findViewById(R.id.register_button);

                description.setText(base.getDescription());
                supervisor.setText("Руководитель: " + base.getSupervisorName());
                participants.setText(String.format(Locale.getDefault(),
                        "Участников: %d/%d", base.getParticipantsCount(), base.getCapacity()));

                updateParticipantsList(base.getParticipants());

                boolean isRegistered = base.getParticipants().stream()
                        .anyMatch(user -> user.getId() == currentUserId);

                if (isRegistered) {
                    registerButton.setText("Отписаться");
                    registerButton.setEnabled(true);
                    registerButton.setOnClickListener(v -> {
                        if (unregisterClickListener != null) {
                            unregisterClickListener.onUnregisterClick(base);
                        }
                    });
                } else {
                    registerButton.setText("Записаться");
                    registerButton.setEnabled(base.getParticipantsCount() < base.getCapacity());
                    registerButton.setOnClickListener(v -> {
                        if (registerClickListener != null) {
                            registerClickListener.onRegisterClick(base);
                        }
                    });
                }
            }
        }

        private void updateParticipantsList(List<User> participants) {
            participantsListContainer.removeAllViews();

            if (participants != null && !participants.isEmpty()) {
                for (User participant : participants) {
                    TextView participantView = new TextView(expandedView.getContext());
                    participantView.setText(participant.getUsername());
                    participantView.setTextSize(14);
                    participantView.setPadding(0, 8, 0, 8);
                    participantsListContainer.addView(participantView);
                }
            } else {
                TextView emptyView = new TextView(expandedView.getContext());
                emptyView.setText("Нет участников");
                emptyView.setTextSize(14);
                emptyView.setPadding(0, 8, 0, 8);
                participantsListContainer.addView(emptyView);
            }

            // Ensure list visibility matches expansion state
            participantsListContainer.setVisibility(isParticipantsExpanded ? View.VISIBLE : View.GONE);
        }

        private void toggleExpand() {
            isExpanded = !isExpanded;
            expandedView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            binding.expandIcon.setRotation(isExpanded ? 180 : 0);

            if (!isExpanded) {
                isParticipantsExpanded = false;
                participantsListContainer.setVisibility(View.GONE);
                participantsExpandIcon.setRotation(0);
            }
        }

        private void toggleParticipantsExpand() {
            isParticipantsExpanded = !isParticipantsExpanded;
            participantsListContainer.setVisibility(isParticipantsExpanded ? View.VISIBLE : View.GONE);
            participantsExpandIcon.setRotation(isParticipantsExpanded ? 180 : 0);
        }
    }
}