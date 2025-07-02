// File: src/main/java/com/example/tradeup/ui/offers/OffersViewModel.java

package com.example.tradeup.ui.offers;

import android.util.Log;
import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OffersViewModel extends ViewModel {

    private static final String TAG = "OffersViewModel";

    private final OfferRepository offerRepository;
    private final AuthRepository authRepository;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;

    private final String currentUserId;

    private final MutableLiveData<Event<Offer>> _openCounterOfferDialogEvent = new MutableLiveData<>();
    public LiveData<Event<Offer>> getOpenCounterOfferDialogEvent() { return _openCounterOfferDialogEvent; }

    private final MutableLiveData<List<Offer>> _receivedOffers = new MutableLiveData<>();
    public LiveData<List<Offer>> getReceivedOffers() { return _receivedOffers; }

    private final MutableLiveData<List<Offer>> _sentOffers = new MutableLiveData<>();
    public LiveData<List<Offer>> getSentOffers() { return _sentOffers; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }

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
        this.currentUserId = (user != null) ? user.getUid() : null;

        loadAllOffers();
    }

    public void loadAllOffers() {
        if (currentUserId == null) {
            _toastMessage.setValue(new Event<>("Please login to see offers."));
            return;
        }
        _isLoading.setValue(true);
        // Tải đồng thời cả hai danh sách
        loadReceivedOffers();
        loadSentOffers();
    }

    private void loadReceivedOffers() {
        offerRepository.getOffersForSeller(currentUserId, new Callback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> data) {
                _receivedOffers.postValue(data != null ? data : Collections.emptyList());
                // Chỉ set isLoading = false sau khi cả hai lệnh gọi có thể đã hoàn thành
                // Một cách đơn giản là set trong cả hai, nhưng nó có thể bị gọi 2 lần
                if (Boolean.TRUE.equals(_isLoading.getValue())) {
                    _isLoading.postValue(false);
                }
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error loading received offers: " + e.getMessage()));
                _isLoading.postValue(false);
            }
        });
    }

    private void loadSentOffers() {
        offerRepository.getOffersByBuyer(currentUserId, new Callback<List<Offer>>() {
            @Override
            public void onSuccess(List<Offer> data) {
                _sentOffers.postValue(data != null ? data : Collections.emptyList());
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _toastMessage.postValue(new Event<>("Error loading sent offers: " + e.getMessage()));
            }
        });
    }

    /**
     * Bắt đầu chuỗi hành động khi chấp nhận một offer.
     * @param offer Offer được chấp nhận.
     */
    public void acceptOffer(Offer offer) {
        _isLoading.setValue(true);
        Log.d("ACCEPT_FLOW", "Step 1: Marking item as sold... Item ID: " + offer.getItemId());
        itemRepository.updateItemStatus(offer.getItemId(), "sold", new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d("ACCEPT_FLOW", "Step 1 SUCCESS. Starting Step 2: Updating offer to accepted...");
                updateOfferToAccepted(offer);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Error: Could not mark item as sold. " + e.getMessage()));
                Log.e("ACCEPT_FLOW", "Step 1 FAILED: Could not mark item as sold.", e);
            }
        });
    }

    private void updateOfferToAccepted(Offer offer) {
        offerRepository.updateOfferStatus(offer.getOfferId(), "accepted", null, null, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d("ACCEPT_FLOW", "Step 2 SUCCESS. Starting Step 3: Creating transaction record...");
                createTransactionRecord(offer);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Item sold, but failed to update offer status."));
                Log.e("ACCEPT_FLOW", "Step 2 FAILED: Could not update offer status.", e);
                loadAllOffers();
            }
        });
    }

    private void createTransactionRecord(Offer acceptedOffer) {
        Log.d("ACCEPT_FLOW", "Step 3.1: Fetching full item details for transaction...");
        itemRepository.getItemById(acceptedOffer.getItemId(), new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item == null) {
                    _isLoading.postValue(false);
                    _toastMessage.postValue(new Event<>("Critical Error: Item sold, but could not find item to create transaction."));
                    Log.e("ACCEPT_FLOW", "Step 3 FAILED: Item " + acceptedOffer.getItemId() + " not found after being sold.");
                    loadAllOffers();
                    return;
                }

                // <<< THÊM LOG DEBUG Ở ĐÂY >>>
                Log.d("TRANSACTION_DEBUG", "Creating Transaction with aAcceptedOffer.buyerId = " + acceptedOffer.getBuyerId());
                Log.d("TRANSACTION_DEBUG", "Creating Transaction with item.sellerId = " + item.getSellerId());
                Log.d("TRANSACTION_DEBUG", "Creating Transaction with item.title = " + item.getTitle());
                Log.d("TRANSACTION_DEBUG", "Creating Transaction with acceptedOffer.price = " + acceptedOffer.getOfferedPrice());

                // <<< KẾT THÚC LOG DEBUG >>>


                Transaction newTransaction = new Transaction();
                newTransaction.setItemId(item.getItemId());
                newTransaction.setItemTitle(item.getTitle());
                newTransaction.setItemImageUrl(
                        (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) ? item.getImageUrls().get(0) : null
                );
                newTransaction.setSellerId(item.getSellerId());
                newTransaction.setBuyerId(acceptedOffer.getBuyerId());
                newTransaction.setPriceSold(acceptedOffer.getOfferedPrice());

                transactionRepository.createTransaction(newTransaction, new Callback<String>() {
                    @Override
                    public void onSuccess(String transactionId) {
                        _isLoading.postValue(false);
                        _toastMessage.postValue(new Event<>("Offer accepted! Transaction completed."));
                        Log.d("ACCEPT_FLOW", "Step 3.3 SUCCESS. Transaction created with ID: " + transactionId);
                        loadAllOffers();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        _isLoading.postValue(false);
                        _toastMessage.postValue(new Event<>("Item sold, but failed to create transaction record."));
                        Log.e("ACCEPT_FLOW", "Step 3.3 FAILED: Could not create transaction record.", e);
                        loadAllOffers();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Item sold, but failed to fetch item details for transaction."));
                Log.e("ACCEPT_FLOW", "Step 3.1 FAILED: Could not fetch item details.", e);
                loadAllOffers();
            }
        });
    }

    public void rejectOffer(Offer offer) {
        updateOfferStatus(offer, "rejected");
    }

    public void counterOffer(Offer offer) {
        _openCounterOfferDialogEvent.setValue(new Event<>(offer));
    }

    public void sendCounterOffer(String offerId, double counterPrice, String message) {
        _isLoading.setValue(true);
        offerRepository.updateOfferStatus(offerId, "countered", counterPrice, message, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Counter offer sent successfully!"));
                loadAllOffers();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to send counter offer: " + e.getMessage()));
            }
        });
    }

    private void updateOfferStatus(Offer offer, String newStatus) {
        _isLoading.setValue(true);
        offerRepository.updateOfferStatus(offer.getOfferId(), newStatus, null, null, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _toastMessage.postValue(new Event<>("Offer status updated to '" + newStatus + "'."));
                loadAllOffers();
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to update offer: " + e.getMessage()));
            }
        });
    }

    public void createOffer(Item item, double offeredPrice, String message) {
        if (currentUserId == null || item == null) {
            _toastMessage.setValue(new Event<>("Error: Missing required information."));
            return;
        }
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        String buyerDisplayName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "A buyer";
        String buyerProfilePic = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;

        Offer newOffer = new Offer();
        newOffer.setItemId(item.getItemId());
        newOffer.setSellerId(item.getSellerId());
        newOffer.setBuyerId(currentUserId);
        newOffer.setOfferedPrice(offeredPrice);
        newOffer.setMessage(message);
        newOffer.setBuyerDisplayName(buyerDisplayName);
        newOffer.setBuyerProfilePictureUrl(buyerProfilePic);
        newOffer.setStatus("pending");

        offerRepository.createOffer(newOffer, new Callback<String>() {
            @Override
            public void onSuccess(String offerId) {
                itemRepository.incrementItemOffers(item.getItemId());
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Offer sent successfully!"));
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                _isLoading.postValue(false);
                _toastMessage.postValue(new Event<>("Failed to send offer: " + e.getMessage()));
            }
        });
    }
}