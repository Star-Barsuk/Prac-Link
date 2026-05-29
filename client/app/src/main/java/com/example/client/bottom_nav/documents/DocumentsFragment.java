package com.example.client.bottom_nav.documents;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.client.bottom_nav.BaseFragment;
import com.example.client.databinding.FragmentDocumentsBinding;
import com.example.client.models.DocumentStatus;
import com.example.client.adapters.DocumentPracticeAdapter;
import com.example.client.models.User;
import com.example.client.network.DocumentRequests;
import com.example.client.network.PracticeRequests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentsFragment extends BaseFragment<FragmentDocumentsBinding> {

    private DocumentPracticeAdapter adapter;
    private static final int PICK_FILE_REQUEST = 1001;

    private int currentUserId = -1;
    private final Map<Integer, List<DocumentStatus>> userDocuments = new HashMap<>();

    private boolean practicesLoaded = false;
    private boolean documentsLoaded = false;

    @Override
    protected FragmentDocumentsBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDocumentsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = userViewModel.getCurrentUser().getValue();
        if (user != null) {
            currentUserId = user.getId();
        }

        setupAdapter();
        loadData();
    }

    private void setupAdapter() {
        adapter = new DocumentPracticeAdapter(
                requireContext(),
                new ArrayList<>(),
                userDocuments,
                this::onDocumentClick
        );
        binding.documentsLv.setAdapter(adapter);
    }

    private void loadData() {
        setLoadingState(true);
        practicesLoaded = false;
        documentsLoaded = false;

        loadUserPractices();
        loadUserDocuments();
    }

    private void loadUserPractices() {
        PracticeRequests.getUserPracticeBases(requireContext(), currentUserId,
                (success, message, bases) -> requireActivity().runOnUiThread(() -> {
                    practicesLoaded = true;
                    if (success && bases != null && !bases.isEmpty()) {
                        adapter.updateList(bases);
                        binding.emptyStateTv.setVisibility(View.GONE);
                        binding.documentsLv.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                    checkAllLoaded();
                }));
    }

    private void loadUserDocuments() {
        DocumentRequests.getUserDocuments(requireContext(), currentUserId,
                (success, message, documents) -> requireActivity().runOnUiThread(() -> {
                    documentsLoaded = true;

                    userDocuments.clear();
                    if (success && documents != null) {
                        for (DocumentStatus doc : documents) {
                            userDocuments.computeIfAbsent(doc.getPracticeBaseId(), k -> new ArrayList<>())
                                    .add(doc);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    checkAllLoaded();
                }));
    }

    private void checkAllLoaded() {
        if (practicesLoaded && documentsLoaded) {
            setLoadingState(false);
        }
    }

    private void showEmptyState() {
        binding.emptyStateTv.setVisibility(View.VISIBLE);
        binding.documentsLv.setVisibility(View.GONE);
    }

    private void onDocumentClick(int practiceBaseId, String documentType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Выберите документ"), PICK_FILE_REQUEST);

        // Сохраняем текущие данные для загрузки
        this.currentPracticeId = practiceBaseId;   // нужно добавить поле
        this.currentDocumentType = documentType;
    }

    private int currentPracticeId = -1;
    private String currentDocumentType = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            uploadDocument(currentPracticeId, currentDocumentType, uri);
        }
    }

    private void uploadDocument(int practiceBaseId, String documentType, Uri fileUri) {
        setLoadingState(true);

        DocumentRequests.uploadDocument(requireContext(), practiceBaseId, currentUserId, documentType, fileUri,
                (success, message) -> requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(requireContext(), "Документ успешно загружен", Toast.LENGTH_SHORT).show();
                        loadData(); // обновляем список
                    } else {
                        Toast.makeText(requireContext(), "Ошибка загрузки: " + message, Toast.LENGTH_LONG).show();
                    }
                }));
    }
}