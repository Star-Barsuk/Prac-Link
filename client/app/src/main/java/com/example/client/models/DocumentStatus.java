package com.example.client.models;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DocumentStatus {

    private int id;
    private int practiceBaseId;
    private int userId;
    private String documentType;
    private String fileName;
    private String filePath;
    private Date uploadedAt;

    public DocumentStatus(int id, int practiceBaseId, int userId, String documentType,
                          String fileName, String filePath, Date uploadedAt) {
        this.id = id;
        this.practiceBaseId = practiceBaseId;
        this.userId = userId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    public int getPracticeBaseId() { return practiceBaseId; }
    public String getDocumentType() { return documentType; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }

    @SuppressLint("ConstantLocale")
    public String getUploadedAtFormatted() {
        if (uploadedAt == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(uploadedAt);
    }

    public static DocumentStatus fromJson(JSONObject json) {
        String dateStr = json.optString("uploaded_at", "");
        Date uploadedAt = Message.parseDate(dateStr);

        return new DocumentStatus(
                json.optInt("id", 0),
                json.optInt("practice_base_id", 0),
                json.optInt("user_id", 0),
                json.optString("document_type", ""),
                json.optString("file_name", ""),
                json.optString("file_path", ""),
                uploadedAt
        );
    }
}