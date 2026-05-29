package com.example.client.network;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import com.example.client.models.DocumentStatus;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class DocumentRequests extends ApiRequest {

    private static final String TAG = DocumentRequests.class.getSimpleName();

    public interface DocumentCallback {
        void onResponse(boolean success, String message);
    }

    public static void uploadDocument(Context context,
                                      int practiceBaseId,
                                      int userId,
                                      String documentType,
                                      Uri fileUri,
                                      DocumentCallback callback) {

        String url = ServerConfig.BASE_URL + "/documents/upload";

        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) {
                callback.onResponse(false, "Не удалось открыть файл");
                return;
            }

            byte[] fileBytes = readAllBytes(inputStream);
            String fileName = getFileNameFromUri(context, fileUri);

            RequestBody fileBody = RequestBody.create(
                    fileBytes,
                    MediaType.parse("application/octet-stream")
            );

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("practice_base_id", String.valueOf(practiceBaseId))
                    .addFormDataPart("user_id", String.valueOf(userId))
                    .addFormDataPart("document_type", documentType)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();

            sendMultipartRequest(context, url, requestBody, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing upload", e);
            callback.onResponse(false, "Ошибка подготовки файла: " + e.getMessage());
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[8192];

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        }
    }

    private static String getFileNameFromUri(Context context, Uri uri) {
        String result = "document.pdf";
        try {
            if ("content".equals(uri.getScheme())) {
                try (android.database.Cursor cursor = context.getContentResolver().query(
                        uri, null, null, null, null)) {

                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) {
                            result = cursor.getString(nameIndex);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return result;
    }

    public static void getUserDocuments(Context context, int userId, UserDocumentsCallback callback) {
        String url = ServerConfig.BASE_URL + "/documents/user/" + userId;

        sendRequest(context, url, null, "GET",
                (success, message, response) -> runOnUiThread(context, () -> {
                    if (!success) {
                        callback.onResponse(false, message, null);
                        return;
                    }

                    try {
                        List<DocumentStatus> documents = new ArrayList<>();
                        org.json.JSONObject root = new org.json.JSONObject(response);
                        org.json.JSONArray array = root.getJSONArray("documents");

                        for (int i = 0; i < array.length(); i++) {
                            DocumentStatus doc = DocumentStatus.fromJson(array.getJSONObject(i));
                            if (doc != null) {
                                documents.add(doc);
                            }
                        }
                        callback.onResponse(true, message, documents);
                    } catch (Exception e) {
                        callback.onResponse(false, "Ошибка парсинга", null);
                    }
                }));
    }

    public interface UserDocumentsCallback {
        void onResponse(boolean success, String message, List<DocumentStatus> documents);
    }
}