// Tạo file mới: ui/profile/TransactionViewData.java
package com.example.tradeup.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.data.model.Transaction;

public class TransactionViewData {
    @NonNull
    public final Transaction transaction;
    @Nullable
    public final String partnerName; // Tên của người mua/bán

    public TransactionViewData(@NonNull Transaction transaction, @Nullable String partnerName) {
        this.transaction = transaction;
        this.partnerName = partnerName;
    }
}