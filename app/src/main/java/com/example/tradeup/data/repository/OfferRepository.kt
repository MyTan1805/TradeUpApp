package com.example.tradeup.data.repository

import com.example.tradeup.data.model.Offer

interface OfferRepository {
    suspend fun createOffer(offer: Offer): Result<String> // Trả về offerId
    suspend fun getOfferById(offerId: String): Result<Offer?>
    suspend fun getOffersForItem(itemId: String): Result<List<Offer>>
    suspend fun getOffersByBuyer(buyerId: String): Result<List<Offer>>
    suspend fun updateOfferStatus(offerId: String, newStatus: String, counterPrice: Double? = null, counterMessage: String? = null): Result<Unit>
}