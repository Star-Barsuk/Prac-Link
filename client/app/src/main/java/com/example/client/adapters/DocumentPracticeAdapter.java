package com.example.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.client.adapters.BaseListAdapter;
import com.example.client.databinding.ItemDocumentPracticeBinding;
import com.example.client.models.PracticeBase;
import com.example.client.models.DocumentStatus;

import java.util.List;
import java.util.Map;

public class DocumentPracticeAdapter extends BaseListAdapter<
        PracticeBase, ItemDocumentPracticeBinding, DocumentPracticeAdapter.ViewHolder> {

    private final OnDocumentClickListener listener;
    private final Map<Integer, List<DocumentStatus>> userDocuments;

    public interface OnDocumentClickListener {
        void onDocumentClick(int practiceBaseId, String documentType);
    }

    public DocumentPracticeAdapter(@NonNull Context context,
                                   @NonNull List<PracticeBase> items,
                                   Map<Integer, List<DocumentStatus>> userDocuments,
                                   OnDocumentClickListener listener) {
        super(context, items);
        this.userDocuments = userDocuments != null ? userDocuments : Map.of();
        this.listener = listener;
    }

    @NonNull
    @Override
    protected ItemDocumentPracticeBinding createBinding(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return ItemDocumentPracticeBinding.inflate(inflater, parent, false);
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull ItemDocumentPracticeBinding binding) {
        return new ViewHolder(binding);
    }

    @Override
    protected void bindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends BaseViewHolder<ItemDocumentPracticeBinding> {

        ViewHolder(@NonNull ItemDocumentPracticeBinding binding) {
            super(binding);
        }

        void bind(PracticeBase base) {
            binding.practiceNameTv.setText(base.getName());

            List<DocumentStatus> docs = userDocuments.get(base.getId());

            updateDocumentStatus(
                    binding.tvAntipLagiatStatus,
                    binding.btnAntipLagiat,
                    findDocument(docs, "антиплагиат")
            );

            updateDocumentStatus(
                    binding.tvPoyasnitelnayaStatus,
                    binding.btnPoyasnitelnaya,
                    findDocument(docs, "пояснительная")
            );

            binding.btnAntipLagiat.setOnClickListener(v ->
                    listener.onDocumentClick(base.getId(), "антиплагиат"));

            binding.btnPoyasnitelnaya.setOnClickListener(v ->
                    listener.onDocumentClick(base.getId(), "пояснительная"));
        }

        private void updateDocumentStatus(TextView statusTv, Button button, DocumentStatus doc) {
            if (doc != null) {
                button.setVisibility(View.GONE);
                statusTv.setVisibility(View.VISIBLE);
                statusTv.setText("✓ " + doc.getFileName() + "\n" + doc.getUploadedAtFormatted());
            } else {
                button.setVisibility(View.VISIBLE);
                statusTv.setVisibility(View.GONE);
            }
        }

        private DocumentStatus findDocument(List<DocumentStatus> docs, String type) {
            if (docs == null) return null;
            for (DocumentStatus d : docs) {
                if (d.getDocumentType().equalsIgnoreCase(type)) {
                    return d;
                }
            }
            return null;
        }
    }
}