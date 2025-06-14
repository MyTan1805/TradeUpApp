package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.Report; // Model Report (Java)
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Inject;

public class FirebaseReportSource {

    private final CollectionReference reportsCollection;

    @Inject
    public FirebaseReportSource(FirebaseFirestore firestore) {
        this.reportsCollection = firestore.collection("reports");
    }

    public Task<Void> submitReport(@NonNull Report report) {
        // Firestore sẽ tự xử lý @ServerTimestamp trong POJO Report khi thêm mới
        // và tự tạo ID cho document khi dùng add()
        return reportsCollection.add(report).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // add() trả về DocumentReference, chúng ta chỉ cần biết nó thành công hay không
            // nên chuyển Task<DocumentReference> thành Task<Void>
            return com.google.android.gms.tasks.Tasks.forResult(null);
        });
    }

    // Các hàm quản lý report (xem, cập nhật status) thường dành cho Admin Panel,
    // không nên có ở client app trừ khi có yêu cầu đặc biệt.
}