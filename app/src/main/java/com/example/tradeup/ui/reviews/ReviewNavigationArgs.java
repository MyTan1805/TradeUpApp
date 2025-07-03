// Gợi ý đặt tại: com/example/tradeup/ui/reviews/ReviewNavigationArgs.java
package com.example.tradeup.ui.reviews;

public class ReviewNavigationArgs {
    public final String transactionId;
    public final String ratedUserId;
    public final String itemId;

    public ReviewNavigationArgs(String transactionId, String ratedUserId, String itemId) {
        this.transactionId = transactionId;
        this.ratedUserId = ratedUserId;
        this.itemId = itemId;
    }
}