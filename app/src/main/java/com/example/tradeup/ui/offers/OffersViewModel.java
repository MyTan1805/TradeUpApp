// File: src/main/java/com/example/tradeup/ui/offers/OffersViewModel.java
// << PHIÊN BẢN ĐÃ SỬA LỖI >>

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

import com.example.tradeup.data.model.Notification;
import com.example.tradeup.data.repository.NotificationRepository;


@HiltViewModel
public class OffersViewModel extends ViewModel {
    private static final String TAG = "OffersViewModel";

    private final OfferRepository offerRepository;
    private final AuthRepository authRepository;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    private final String currentUserId;

    private final MutableLiveData<List<Offer>> _receivedOffers = new MutableLiveData<>();
    public LiveData<List<Offer>> getReceivedOffers() { return _receivedOffers; }

    private final MutableLiveData<List<Offer>> _sentOffers = new MutableLiveData<>();
    public LiveData<List<Offer>> getSentOffers() { return _sentOffers; }

    // Các LiveData khác giữ nguyên...
    private final MutableLiveData<List<Transaction>> _transactions = new MutableLiveData<>();
    public LiveData<List<Transaction>> getTransactions() { return _transactions; }
    private final MutableLiveData<Event<Offer>> _openCounterOfferDialogEvent = new MutableLiveData<>();
    public LiveData<Event<Offer>> getOpenCounterOfferDialogEvent() { return _openCounterOfferDialogEvent; }
    private final MutableLiveData<Event<Transaction>> _openPaymentSelectionEvent = new MutableLiveData<>();
    public LiveData<Event<Transaction>> getOpenPaymentSelectionEvent() { return _openPaymentSelectionEvent; }
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() { return _isLoading; }
    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> getToastMessage() { return _toastMessage; }


    @Inject
    public OffersViewModel(
            OfferRepository offerRepository,
            AuthRepository authRepository,
            ItemRepository itemRepository,
            TransactionRepository transactionRepository,
            NotificationRepository notificationRepository
    ) {
        this.offerRepository = offerRepository;
        this.authRepository = authRepository;
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;

        FirebaseUser user = authRepository.getCurrentUser();
        this.currentUserId = user != null ? user.getUid() : null;

        loadInitialData();
    }

    public void accept(Offer offer) {
        if (!validateOfferAction(offer)) return;

        _isLoading.setValue(true);

        itemRepository.updateItemStatus(offer.getItemId(), "sold", new Callback<Void>() {
            @Override
            public void onSuccess(Void itemStatusData) {
                updateOfferStatusAndCreateTransaction(offer);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Could not mark item as sold: " + e.getMessage());
            }
        });
    }

    // << HÀM MỚI: Tách logic để rõ ràng hơn >>
    private void updateOfferStatusAndCreateTransaction(Offer offer) {
        // Luôn chấp nhận giá `currentPrice`
        offerRepository.updateOffer(offer.getOfferId(), "accepted", offer.getCurrentPrice(), "Offer accepted.", new Callback<Void>() {
            @Override
            public void onSuccess(Void offerStatusData) {
                String receiverId = offer.getLastActionByUid(); // Người đưa ra giá cuối cùng sẽ nhận thông báo bị chấp nhận
                sendOfferStatusNotification(offer, "offer_accepted", receiverId);
                createTransactionFromOffer(offer);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to update offer status: " + e.getMessage());
            }
        });
    }
    // << HÀM CREATETRANSACTIONFROMOFFER ĐƯỢC CẬP NHẬT >>
    // Hàm này bây giờ chỉ tập trung vào việc tạo transaction, không mở dialog nữa
    private void createTransactionFromOffer(Offer offer) {
        itemRepository.getItemById(offer.getItemId(), new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item == null) {
                    showError("Item not found, cannot create transaction.");
                    return;
                }

                Transaction transaction = createTransactionObject(offer, item);
                transactionRepository.createTransaction(transaction, new Callback<String>() {
                    @Override
                    public void onSuccess(String transactionId) {
                        // << SỬA Ở ĐÂY >>
                        // Chỉ hiển thị thông báo thành công cho người bán.
                        showSuccess("Offer accepted! The buyer has been notified.");
                        // Tải lại toàn bộ danh sách để cập nhật giao diện
                        loadAllOffers();
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showError("Failed to create transaction: " + e.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to get item details for transaction: " + e.getMessage());
            }
        });
    }

    // << HÀM SENDOFFERSTATUSNOTIFICATION ĐƯỢC CẬP NHẬT >>
    private void sendOfferStatusNotification(Offer offer, String type, String receiverId) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) return;

        // Lấy tên sản phẩm để hiển thị trong thông báo (cần lấy từ item)
        itemRepository.getItemById(offer.getItemId(), new Callback<Item>() {
            @Override
            public void onSuccess(Item item) {
                if (item == null) return;

                Notification notif = new Notification();
                notif.setUserId(receiverId);
                notif.setType(type);
                notif.setRelatedContentId(offer.getItemId());
                notif.setRead(false);

                // Lấy ảnh của sản phẩm
                if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                    notif.setImageUrl(item.getImageUrls().get(0));
                }

                // Tùy chỉnh title và message dựa trên type
                switch (type) {
                    case "offer_accepted":
                        notif.setTitle("Offer Accepted!");
                        notif.setMessage("Your offer for '" + item.getTitle() + "' has been accepted. Tap to proceed to payment.");
                        break;
                    case "offer_rejected":
                        notif.setTitle("Offer Rejected");
                        notif.setMessage("Your offer for '" + item.getTitle() + "' has been rejected by the seller.");
                        break;
                    case "offer_countered":
                        notif.setTitle("New Counter Offer");
                        notif.setMessage("You've received a counter offer for '" + item.getTitle() + "'.");
                        break;
                }

                // Gửi thông báo sau khi đã có đủ thông tin
                notificationRepository.createNotification(notif, new Callback<Void>() {
                    @Override public void onSuccess(Void data) { Log.d(TAG, "Sent offer status notification successfully for type: " + type); }
                    @Override public void onFailure(@NonNull Exception e) { Log.e(TAG, "Failed to send offer status notification for type: " + type, e); }
                });
            }
            @Override public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Could not get item details to create notification", e);
            }
        });
    }


    // =================================================================
    // === CÁC HÀM CÒN LẠI GIỮ NGUYÊN ===================================
    // =================================================================

    private void loadInitialData() {
        if (currentUserId == null) {
            showError("Please login to see data");
            return;
        }
        _isLoading.setValue(true);
        loadAllOffers();
        loadTransactions(false);
    }

    public void createOffer(Item item, double offeredPrice, String message) {
        if (!validateOfferCreation(item)) return;
        _isLoading.setValue(true);

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            showError("User not logged in.");
            return;
        }

        Offer newOffer = new Offer();
        newOffer.setItemId(item.getItemId());
        newOffer.setSellerId(item.getSellerId());
        newOffer.setBuyerId(currentUser.getUid());
        newOffer.setBuyerDisplayName(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "A buyer");
        if (currentUser.getPhotoUrl() != null) {
            newOffer.setBuyerProfilePictureUrl(currentUser.getPhotoUrl().toString());
        }

        newOffer.setCurrentPrice(offeredPrice);
        newOffer.setLastMessage(message);
        newOffer.setLastActionByUid(currentUser.getUid()); // Người mua là người hành động cuối
        newOffer.setStatus("pending");
        newOffer.setExpiresAt(new Timestamp(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)));

        offerRepository.createOffer(newOffer, new Callback<String>() {
            @Override
            public void onSuccess(String offerId) {
                handleOfferCreationSuccess(item);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to send offer: " + e.getMessage());
            }
        });
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

    private void handleOfferCreationSuccess(Item item) {
        itemRepository.incrementItemOffers(item.getItemId());

        sendNewOfferNotification(item);

        showSuccess("Offer sent successfully!");
        loadSentOffers();
    }

    private void sendNewOfferNotification(Item item) {
        FirebaseUser buyer = authRepository.getCurrentUser();
        if (buyer == null) return;

        Notification notif = new Notification();
        notif.setUserId(item.getSellerId()); // Người nhận là người bán
        notif.setType("new_offer");
        notif.setTitle("New Offer Received!");

        String buyerName = buyer.getDisplayName() != null ? buyer.getDisplayName() : "A user";
        String message = buyerName + " has sent you an offer for '" + item.getTitle() + "'.";
        notif.setMessage(message);

        // Lấy ảnh đầu tiên của sản phẩm làm ảnh thông báo
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            notif.setImageUrl(item.getImageUrls().get(0));
        }
        notif.setRelatedContentId(item.getItemId()); // ID để điều hướng
        notif.setRead(false);

        notificationRepository.createNotification(notif, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Successfully created notification for new offer.");
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to create notification for new offer.", e);
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

        if (!isSeller && !isBuyer) {
            showError("You are not involved in this offer");
            return false;
        }

        return true;
    }

    public void reject(Offer offer) {
        if (!validateOfferAction(offer)) return;

        String receiverId = offer.getSellerId().equals(currentUserId)
                ? offer.getBuyerId()
                : offer.getSellerId();

        updateOfferStatusAndNotify(offer, "rejected", receiverId);
    }

    public void counter(Offer offer) {
        _openCounterOfferDialogEvent.setValue(new Event<>(offer));
    }

    public void submitCounterOffer(String offerId, double newPrice, String message) {
        _isLoading.setValue(true);
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null) {
            showError("User not logged in.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentPrice", newPrice);
        updates.put("lastMessage", message);
        updates.put("lastActionByUid", currentUser.getUid()); // Ghi nhận người vừa trả giá
        updates.put("status", "pending"); // Đảm bảo trạng thái vẫn là pending
        updates.put("updatedAt", FieldValue.serverTimestamp());

        offerRepository.updateOffer(offerId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                offerRepository.getOfferById(offerId, new Callback<Offer>() {
                    @Override
                    public void onSuccess(Offer updatedOffer) {
                        if (updatedOffer != null) {
                            // Gửi thông báo cho người còn lại
                            String receiverId = updatedOffer.getSellerId().equals(currentUser.getUid())
                                    ? updatedOffer.getBuyerId()
                                    : updatedOffer.getSellerId();
                            sendOfferStatusNotification(updatedOffer, "offer_countered", receiverId);
                        }
                    }
                    @Override public void onFailure(@NonNull Exception e) { Log.e(TAG, "Failed to get updated offer", e); }
                });
                showSuccess("Counter offer sent!");
                loadAllOffers();
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to send counter offer: " + e.getMessage());
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
        // Giá chốt đơn LUÔN LÀ `currentPrice`
        transaction.setPriceSold(offer.getCurrentPrice());
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

        if ("COD".equalsIgnoreCase(paymentMethod)) {
            if (deliveryAddress != null) updates.put("deliveryAddress", deliveryAddress);
            updates.put("shippingStatus", "waiting_for_shipment"); // << SET TRẠNG THÁI BAN ĐẦU
        }

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Payment method selected.");
                loadTransactions(true); // Tải lại để thấy trạng thái mới
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Payment selection failed: " + e.getMessage());
            }
        });
    }

    public void markAsShipped(String transactionId) {
        _isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("shippingStatus", "shipped");
        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Marked as shipped.");
                loadTransactions(false); // Giả sử seller đang ở tab Sales
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to update status: " + e.getMessage());
            }
        });
    }

    public void confirmReceipt(String transactionId) {
        _isLoading.setValue(true);
        // Khi người mua xác nhận, giao dịch hoàn tất
        Map<String, Object> updates = new HashMap<>();
        updates.put("shippingStatus", "completed");
        updates.put("paymentStatus", "completed");
        updates.put("transactionDate", FieldValue.serverTimestamp());

        transactionRepository.updateTransaction(transactionId, updates, new Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showSuccess("Transaction completed!");
                loadTransactions(true); // Giả sử buyer đang ở tab Purchases
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                showError("Failed to complete transaction: " + e.getMessage());
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

    private void updateOfferStatusAndNotify(Offer offer, String newStatus, String receiverId) {
        _isLoading.setValue(true);
        offerRepository.updateOffer(offer.getOfferId(), newStatus, null, null, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                sendOfferStatusNotification(offer, newStatus, receiverId);
                showSuccess("Offer status updated.");
                loadAllOffers(); // Tải lại danh sách
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
}