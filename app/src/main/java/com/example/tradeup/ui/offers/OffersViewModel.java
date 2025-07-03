package com.example.tradeup.ui.offers;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.core.utils.Event;
import com.example.tradeup.data.model.Item;
import com.example.tradeup.data.model.Offer;
import com.example.tradeup.data.model.Transaction;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.OfferRepository;
import com.example.tradeup.data.repository.TransactionRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OffersViewModel extends ViewModel {
    private static final String TAG = "OffersViewModel";

    // Repositories
    private final OfferRepository offerRepository;
    private final AuthRepository authRepository;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    private final String currentUserId;

    // LiveData for UI
    private final MutableLiveData<List<Offer>> _receivedOffers = new MutableLiveData<>();
    private final MutableLiveData<List<Offer>> _sentOffers = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    private final MutableLiveData<Event<Offer>> _openCounterOfferDialogEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<Transaction>> _openPaymentSelectionEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();

    @Inject
    public OffersViewModel(
            OfferRepository offerRepository,
            AuthRepository authRepository,
            ItemRepository itemRepository,
            TransactionRepository transactionRepository
    ) {
        this.offerRepository = offerRepository;
        this.authRepository = authRepository;
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = user != null ? user.getUid() : null;

        loadInitialData();
    }

    // region Public LiveData Getters
    public LiveData<List<Offer>> getReceivedOffers() { return _receivedOffers; }
    public LiveData<List<Offer>> getSentOffers() { return _sentOffers; }
    public LiveData<List<Transaction>> getTransactions() { return _transactions; }
    public LiveData<Event<Offer>> getOpenCounterOfferDialogEvent() { return _openCounterOfferDialogEvent; }
    public LiveData<Event<Transaction>> getOpenPaymentSelectionEvent() { return _openPaymentSelectionEvent; }
    public LiveData<Boolean> isLoading() { return _isLoading; }
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }
    // endregion

    // region Initialization
    private void loadInitialData() {
        if (currentUserId == null) {
            showError("Please login to see data");
            return;
        }
        _isLoading.setValue(true);
        loadAllOffers();
        loadTransactions(false); // Load as seller by default
    }
    // endregion

    // region Offer Management
    public void createOffer(Item item, double offeredPrice, String message) {
        if (!validateOfferCreation(item)) return;

        _isLoading.setValue(true);

        offerRepository.createOffer(
                createNewOfferObject(item, offeredPrice, message),
                new Callback<String>() {
                    @Override
                    public void onSuccess(String offerId) {
                        handleOfferCreationSuccess(item);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showError("Failed to send offer: " + e.getMessage());
                    }
                }
        );
    }

    private boolean validateOfferCreation(Item item) {
        if (currentUserId == null || item == null) {
            showError("Missing required information");
            return false;
        }

        if (!"available".equalsIgnoreCase(item.getStatus())) {
            showError("Item is not available");
            return false;
        }
        return true;
    }

    private Offer createNewOfferObject(Item item, double offeredPrice, String message) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        Offer newOffer = new Offer();

        newOffer.setItemId(item.getItemId());
        newOffer.setSellerId(item.getSellerId());
        newOffer.setBuyerId(currentUserId);
        newOffer.setBuyerDisplayName(currentUser.getDisplayName() != null ?
                currentUser.getDisplayName() : "A buyer");
        newOffer.setStatus("pending");
        newOffer.setOfferedPrice(offeredPrice);
        newOffer.setMessage(message);
        newOffer.setExpiresAt(new Timestamp(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)));

        if (currentUser.getPhotoUrl() != null) {
            newOffer.setBuyerProfilePictureUrl(currentUser.getPhotoUrl().toString());
        }

        return newOffer;
    }

    private void handleOfferCreationSuccess(Item item) {
        itemRepository.incrementItemOffers(item.getItemId());
        showSuccess("Offer sent successfully!");
        loadSentOffers();
    }

    public void accept(Offer offer) {
        if (!validateOfferAction(offer)) return;

        _isLoading.setValue(true);
        itemRepository.updateItemStatus(offer.getItemId(), "sold", new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                processAcceptedOffer(offer);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Could not mark item as sold: " + e.getMessage());
            }
        });
    }

    private boolean validateOfferAction(Offer offer) {
        if (currentUserId == null) {
            showError("Please login to perform this action");
            return false;
        }

        boolean isSeller = currentUserId.equals(offer.getSellerId());
        boolean isBuyer = currentUserId.equals(offer.getBuyerId());

        if (isSeller && offer.getCounterOfferPrice() != null) {
            showError("Seller cannot accept their own counter-offer");
            return false;
        }

        if (isBuyer && offer.getCounterOfferPrice() == null) {
            showError("Buyer cannot accept their own offer");
            return false;
        }

        if (!isSeller && !isBuyer) {
            showError("You are not involved in this offer");
            return false;
        }

        return true;
    }

    private void processAcceptedOffer(Offer offer) {
        offerRepository.updateOffer(offer.getOfferId(), "accepted", null, "Offer accepted",
                new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        createTransactionFromOffer(offer);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showError("Failed to update offer status: " + e.getMessage());
                    }
                }
        );
    }

    public void reject(Offer offer) {
        updateOfferStatus(offer.getOfferId(), "rejected", null, "Offer rejected");
    }

    public void counter(Offer offer) {
        _openCounterOfferDialogEvent.setValue(new Event<>(offer));
    }

    public void submitCounterOffer(String offerId, double newPrice, String message) {
        _isLoading.setValue(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("counterOfferPrice", newPrice);
        updates.put("counterOfferMessage", message.isEmpty() ? "Sent a new price" : message);
        updates.put("status", "pending");
        updates.put("updatedAt", FieldValue.serverTimestamp());

        offerRepository.updateOffer(offerId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Counter offer sent!");
                loadAllOffers();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to send counter offer: " + e.getMessage());
            }
        });
    }
    // endregion

    // region Transaction Management
    private void createTransactionFromOffer(Offer offer) {
        itemRepository.getItemById(offer.getItemId(), new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item == null) {
                    showError("Item not found for transaction");
                    return;
                }

                Transaction transaction = createTransactionObject(offer, item);
                transactionRepository.createTransaction(transaction, new Callback<String>() {
                    @Override
                    public void onSuccess(String transactionId) {
                        showSuccess("Offer accepted! Please select payment method");
                        _openPaymentSelectionEvent.postValue(new Event<>(transaction));
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showError("Failed to create transaction: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to get item details: " + e.getMessage());
            }
        });
    }

    private Transaction createTransactionObject(Offer offer, Item item) {
        Transaction transaction = new Transaction();
        transaction.setItemId(item.getItemId());
        transaction.setItemTitle(item.getTitle());
        transaction.setItemImageUrl(item.getImageUrls() != null && !item.getImageUrls().isEmpty() ?
                item.getImageUrls().get(0) : null);
        transaction.setSellerId(item.getSellerId());
        transaction.setBuyerId(offer.getBuyerId());
        transaction.setPriceSold(offer.getCounterOfferPrice() != null ?
                offer.getCounterOfferPrice() : offer.getOfferedPrice());
        transaction.setPaymentStatus("pending");
        return transaction;
    }

    public void loadTransactions(boolean asBuyer) {
        if (currentUserId == null) {
            showError("Please login to see transactions");
            return;
        }

        _isLoading.setValue(true);
        String role = asBuyer ? "buyerId" : "sellerId";

        transactionRepository.getTransactionsByUser(currentUserId, role, 50,
                new Callback<List<Transaction>>() {
                    @Override
                    public void onSuccess(List<Transaction> transactions) {
                        Collections.sort(transactions, (t1, t2) ->
                                t2.getTransactionDate().compareTo(t1.getTransactionDate())
                        );
                        _transactions.postValue(transactions);
                        _isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showError("Error loading transactions: " + e.getMessage());
                    }
                }
        );
    }

    public void selectPaymentMethod(String transactionId, String paymentMethod, @Nullable String deliveryAddress) {
        _isLoading.setValue(true);

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentMethod", paymentMethod);
        updates.put("paymentStatus", "pending");

        if (paymentMethod.equals("COD") && deliveryAddress != null) {
            updates.put("deliveryAddress", deliveryAddress);
            updates.put("shippingStatus", "waiting_for_shipment");
        }

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess(paymentMethod.equals("COD") ?
                        "COD selected. Please wait for delivery" :
                        "Processing online payment");
                loadTransactions(false);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Payment selection failed: " + e.getMessage());
            }
        });
    }

    public void confirmCODPayment(String transactionId, boolean isSellerConfirm) {
        _isLoading.setValue(true);

        Map<String, Object> updates = new HashMap<>();
        String field = isSellerConfirm ? "sellerConfirmed" : "buyerConfirmed";
        updates.put(field, true);

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                checkCODConfirmationStatus(transactionId);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Confirmation failed: " + e.getMessage());
            }
        });
    }

    private void checkCODConfirmationStatus(String transactionId) {
        transactionRepository.getTransactionById(transactionId, new Callback<Transaction>() {
            @Override
            public void onSuccess(Transaction transaction) {
                if (transaction.isSellerConfirmed() && transaction.isBuyerConfirmed()) {
                    completeTransaction(transactionId);
                } else {
                    showSuccess("Confirmation recorded");
                    _isLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to verify status: " + e.getMessage());
            }
        });
    }

    private void completeTransaction(String transactionId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "completed");
        updates.put("transactionDate", FieldValue.serverTimestamp());

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Payment confirmed! Transaction completed");
                loadTransactions(false);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to complete transaction: " + e.getMessage());
            }
        });
    }
    // endregion

    // region Helper Methods
    private void loadAllOffers() {
        loadReceivedOffers();
        loadSentOffers();
    }

    private void loadReceivedOffers() {
        offerRepository.getOffersForSeller(currentUserId, new Callback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> offers) {
                processOffers(offers, _receivedOffers);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Error loading received offers: " + e.getMessage());
            }
        });
    }

    private void loadSentOffers() {
        offerRepository.getOffersByBuyer(currentUserId, new Callback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> offers) {
                processOffers(offers, _sentOffers);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Error loading sent offers: " + e.getMessage());
            }
        });
    }

    private void processOffers(List<Offer> offers, MutableLiveData<List<Offer>> targetLiveData) {
        List<Offer> validOffers = new ArrayList<>();

        if (offers != null) {
            for (Offer offer : offers) {
                if (isOfferExpired(offer)) {
                    handleExpiredOffer(offer);
                } else {
                    validOffers.add(offer);
                }
            }
        }

        targetLiveData.postValue(validOffers);
        _isLoading.postValue(false);
    }

    private boolean isOfferExpired(Offer offer) {
        return offer.getExpiresAt() != null &&
                offer.getExpiresAt().toDate().before(new Date()) &&
                "pending".equalsIgnoreCase(offer.getStatus());
    }

    private void handleExpiredOffer(Offer offer) {
        offerRepository.updateOffer(offer.getOfferId(), "expired", null, null, new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Marked expired offer: " + offer.getOfferId());
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to mark expired offer", e);
            }
        });
    }

    private void updateOfferStatus(String offerId, String status, @Nullable Double price, String message) {
        _isLoading.setValue(true);
        offerRepository.updateOffer(offerId, status, price, message, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess(message);
                loadAllOffers();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Action failed: " + e.getMessage());
            }
        });
    }

    private void showError(String message) {
        _toastMessage.postValue(new Event<>(message));
        _isLoading.postValue(false);
    }

    private void showSuccess(String message) {
        _toastMessage.postValue(new Event<>(message));
        _isLoading.postValue(false);
    }
    // endregion
}