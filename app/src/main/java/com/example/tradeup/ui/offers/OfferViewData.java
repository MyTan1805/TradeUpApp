package com.example.tradeup.ui.offers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Offer;
import java.util.Objects;

public class OfferViewData {
    @NonNull
    public final Offer offer;
    @Nullable // Item có thể null nếu không tìm thấy hoặc bị xóa
    public final Item relatedItem;

    public OfferViewData(@NonNull Offer offer, @Nullable Item relatedItem) {
        this.offer = offer;
        this.relatedItem = relatedItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfferViewData that = (OfferViewData) o;
        return offer.getOfferId().equals(that.offer.getOfferId()) &&
                Objects.equals(relatedItem, that.relatedItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offer.getOfferId(), relatedItem);
    }
}