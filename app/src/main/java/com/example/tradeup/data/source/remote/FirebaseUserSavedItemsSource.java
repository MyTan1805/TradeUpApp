package com.example.tradeup.data.source.remote;

import androidx.annotation.NonNull;
import com.example.tradeup.data.model.UserSavedItems; // Model UserSavedItems (Java)
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

public class FirebaseUserSavedItemsSource {

    private final CollectionReference savedItemsCollection;

    @Inject
    public FirebaseUserSavedItemsSource(FirebaseFirestore firestore) {
        this.savedItemsCollection = firestore.collection("userSavedItems");
    }

    public Task<Void> saveItem(@NonNull String userId, @NonNull String itemId) {
        DocumentReference docRef = savedItemsCollection.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("itemIds", FieldValue.arrayUnion(itemId));
        updates.put("updatedAt", FieldValue.serverTimestamp());

        return docRef.update(updates)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return Tasks.forResult(null);
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFirestoreException &&
                                ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.NOT_FOUND) {
                            // Document chưa tồn tại, tạo mới
                            UserSavedItems newSavedItems = new UserSavedItems(userId, Collections.singletonList(itemId));
                            // Dùng Map để đảm bảo serverTimestamp được set đúng cách khi tạo mới
                            return docRef.set(newSavedItems.toMapForSet());
                        }
                        // Ném lại exception gốc nếu không phải lỗi NOT_FOUND
                        throw Objects.requireNonNull(e);
                    }
                });
    }

    public Task<Void> unsaveItem(@NonNull String userId, @NonNull String itemId) {
        DocumentReference docRef = savedItemsCollection.document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("itemIds", FieldValue.arrayRemove(itemId));
        updates.put("updatedAt", FieldValue.serverTimestamp());
        return docRef.update(updates);
    }

    public Task<List<String>> getSavedItemIds(@NonNull String userId) {
        return savedItemsCollection.document(userId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserSavedItems savedItemsDoc = documentSnapshot.toObject(UserSavedItems.class);
                        return (savedItemsDoc != null && savedItemsDoc.getItemIds() != null) ?
                                savedItemsDoc.getItemIds() : Collections.emptyList();
                    }
                    return Collections.emptyList(); // Trả về list rỗng nếu document không tồn tại
                });
    }

    public Task<Boolean> isItemSaved(@NonNull String userId, @NonNull String itemId) {
        return savedItemsCollection.document(userId).get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        // Nếu không lấy được document (ví dụ lỗi mạng), có thể coi là chưa lưu hoặc ném lỗi
                        // Ở đây chúng ta ném lỗi để Repository xử lý
                        throw Objects.requireNonNull(task.getException());
                    }
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserSavedItems savedItemsDoc = documentSnapshot.toObject(UserSavedItems.class);
                        return savedItemsDoc != null &&
                                savedItemsDoc.getItemIds() != null &&
                                savedItemsDoc.getItemIds().contains(itemId);
                    }
                    return false; // Document không tồn tại -> chưa lưu
                });
    }
}