// File: com/example/tradeup/core/utils/TaskToFuture.java
package com.example.tradeup.core.utils;

import com.google.android.gms.tasks.Task;
import java.util.concurrent.CompletableFuture;

public class TaskToFuture {
    public static <T> CompletableFuture<T> toCompletableFuture(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        task.addOnSuccessListener(future::complete)
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}