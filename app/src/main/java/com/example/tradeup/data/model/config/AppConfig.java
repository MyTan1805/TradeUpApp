package com.example.tradeup.data.model.config;

import androidx.annotation.NonNull; // << Thêm NonNull để code rõ ràng hơn
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Collections; // << Import Collections
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {
    private List<CategoryConfig> categories;

    private List<DisplayCategoryConfig> displayCategories;
    private List<ItemConditionConfig> itemConditions;
    private List<ReportReasonConfig> reportReasons;
    private List<PaymentMethodConfig> supportedPaymentMethods;
    private List<Integer> locationSearchRadiusOptions;
    private int maxImageUploadPerItem;
    private String minAppVersionRequired;
    private boolean maintenanceMode;
    @Nullable
    private String maintenanceMessage;
    private CurrencyConfig currency;
    private ContactSupportConfig contactSupport;
    @Nullable
    private String privacyPolicyUrl;
    @Nullable
    private String termsAndConditionsUrl;
    private Map<String, List<String>> suggestedTags;
    @ServerTimestamp
    @Nullable
    private Timestamp lastUpdated;

    // Constructor rỗng đã rất tốt
    public AppConfig() {
        this.categories = new ArrayList<>();
        this.itemConditions = new ArrayList<>();
        this.displayCategories = new ArrayList<>();
        this.reportReasons = new ArrayList<>();
        this.supportedPaymentMethods = new ArrayList<>();
        this.locationSearchRadiusOptions = new ArrayList<>();
        this.suggestedTags = new HashMap<>();
        this.currency = new CurrencyConfig();
        this.contactSupport = new ContactSupportConfig();
    }

    // --- GETTERS AND SETTERS (ĐÃ CẢI TIẾN) ---

    // Getter và Setter cho categories
    @NonNull
    public List<DisplayCategoryConfig> getDisplayCategories() {
        return displayCategories;
    }

    public void setDisplayCategories(List<DisplayCategoryConfig> displayCategories) {
        this.displayCategories = (displayCategories != null) ? displayCategories : Collections.emptyList();
    }

    @NonNull
    public List<CategoryConfig> getCategories() {
        return categories;
    }
    public void setCategories(List<CategoryConfig> categories) {
        this.categories = (categories != null) ? categories : Collections.emptyList();
    }

    // Các getter và setter khác đã được cải tiến tương tự
    @NonNull
    public List<ItemConditionConfig> getItemConditions() {
        return itemConditions;
    }

    public void setItemConditions(List<ItemConditionConfig> itemConditions) {
        this.itemConditions = (itemConditions != null) ? itemConditions : Collections.emptyList();
    }

    @NonNull
    public List<ReportReasonConfig> getReportReasons() {
        return reportReasons;
    }

    public void setReportReasons(List<ReportReasonConfig> reportReasons) {
        this.reportReasons = (reportReasons != null) ? reportReasons : Collections.emptyList();
    }

    @NonNull
    public List<PaymentMethodConfig> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    public void setSupportedPaymentMethods(List<PaymentMethodConfig> supportedPaymentMethods) {
        this.supportedPaymentMethods = (supportedPaymentMethods != null) ? supportedPaymentMethods : Collections.emptyList();
    }

    @NonNull
    public List<Integer> getLocationSearchRadiusOptions() {
        return locationSearchRadiusOptions;
    }

    public void setLocationSearchRadiusOptions(List<Integer> locationSearchRadiusOptions) {
        this.locationSearchRadiusOptions = (locationSearchRadiusOptions != null) ? locationSearchRadiusOptions : Collections.emptyList();
    }

    @NonNull
    public Map<String, List<String>> getSuggestedTags() {
        return suggestedTags;
    }

    public void setSuggestedTags(Map<String, List<String>> suggestedTags) {
        this.suggestedTags = (suggestedTags != null) ? suggestedTags : Collections.emptyMap();
    }

    // Các getter/setter cho kiểu nguyên thủy và Object thì không cần thay đổi
    // Chúng đã hoạt động đúng
    public int getMaxImageUploadPerItem() { return maxImageUploadPerItem; }
    public void setMaxImageUploadPerItem(int maxImageUploadPerItem) { this.maxImageUploadPerItem = maxImageUploadPerItem; }

    public String getMinAppVersionRequired() { return minAppVersionRequired; }
    public void setMinAppVersionRequired(String minAppVersionRequired) { this.minAppVersionRequired = minAppVersionRequired; }

    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }

    @Nullable public String getMaintenanceMessage() { return maintenanceMessage; }
    public void setMaintenanceMessage(@Nullable String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }

    public CurrencyConfig getCurrency() { return currency; }
    public void setCurrency(CurrencyConfig currency) { this.currency = currency; }

    public ContactSupportConfig getContactSupport() { return contactSupport; }
    public void setContactSupport(ContactSupportConfig contactSupport) { this.contactSupport = contactSupport; }

    @Nullable public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(@Nullable String privacyPolicyUrl) { this.privacyPolicyUrl = privacyPolicyUrl; }

    @Nullable public String getTermsAndConditionsUrl() { return termsAndConditionsUrl; }
    public void setTermsAndConditionsUrl(@Nullable String termsAndConditionsUrl) { this.termsAndConditionsUrl = termsAndConditionsUrl; }

    @Nullable public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(@Nullable Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}