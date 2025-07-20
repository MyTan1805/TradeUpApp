// File: src/main/java/com/example/tradeup/data/model/config/AppConfig.java
package com.example.tradeup.data.model.config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {

    private List<CategoryConfig> categories; // Chỉ giữ lại field này cho danh mục
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

    public AppConfig() {
        this.categories = new ArrayList<>();
        this.itemConditions = new ArrayList<>();
        this.reportReasons = new ArrayList<>();
        this.supportedPaymentMethods = new ArrayList<>();
        this.locationSearchRadiusOptions = new ArrayList<>();
        this.suggestedTags = new HashMap<>();
        this.currency = new CurrencyConfig();
        this.contactSupport = new ContactSupportConfig();
    }

    // --- GETTERS AND SETTERS ---

    @NonNull
    public List<CategoryConfig> getCategories() {
        return categories != null ? categories : Collections.emptyList();
    }
    public void setCategories(List<CategoryConfig> categories) {
        this.categories = categories;
    }

    @NonNull
    public List<ItemConditionConfig> getItemConditions() {
        return itemConditions != null ? itemConditions : Collections.emptyList();
    }
    public void setItemConditions(List<ItemConditionConfig> itemConditions) { this.itemConditions = itemConditions; }

    @NonNull
    public List<ReportReasonConfig> getReportReasons() {
        return reportReasons != null ? reportReasons : Collections.emptyList();
    }
    public void setReportReasons(List<ReportReasonConfig> reportReasons) { this.reportReasons = reportReasons; }

    @NonNull
    public List<PaymentMethodConfig> getSupportedPaymentMethods() {
        return supportedPaymentMethods != null ? supportedPaymentMethods : Collections.emptyList();
    }
    public void setSupportedPaymentMethods(List<PaymentMethodConfig> supportedPaymentMethods) { this.supportedPaymentMethods = supportedPaymentMethods; }

    @NonNull
    public List<Integer> getLocationSearchRadiusOptions() {
        return locationSearchRadiusOptions != null ? locationSearchRadiusOptions : Collections.emptyList();
    }
    public void setLocationSearchRadiusOptions(List<Integer> locationSearchRadiusOptions) { this.locationSearchRadiusOptions = locationSearchRadiusOptions; }

    @NonNull
    public Map<String, List<String>> getSuggestedTags() {
        return suggestedTags != null ? suggestedTags : Collections.emptyMap();
    }
    public void setSuggestedTags(Map<String, List<String>> suggestedTags) { this.suggestedTags = suggestedTags; }

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