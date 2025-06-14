package com.example.tradeup.data.model.config;

import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {
    @DocumentId
    @Nullable
    private String id; // Should always be "global"
    private List<CategoryConfig> categories;
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
    private Map<String, List<String>> suggestedTags; // Key: subcategoryId
    @ServerTimestamp
    @Nullable
    private Timestamp lastUpdated;

    // Constructor rỗng cần thiết cho Firestore
    public AppConfig() {
        this.id = "global";
        this.categories = new ArrayList<>();
        this.itemConditions = new ArrayList<>();
        this.reportReasons = new ArrayList<>();
        this.supportedPaymentMethods = new ArrayList<>();
        this.locationSearchRadiusOptions = Arrays.asList(5, 10, 25, 50, 100);
        this.maxImageUploadPerItem = 10;
        this.minAppVersionRequired = "1.0.0";
        this.maintenanceMode = false;
        this.maintenanceMessage = null;
        this.currency = new CurrencyConfig();
        this.contactSupport = new ContactSupportConfig();
        this.privacyPolicyUrl = null;
        this.termsAndConditionsUrl = null;
        this.suggestedTags = new HashMap<>();
        this.lastUpdated = null;
    }

    // Getters and Setters
    @Nullable
    public String getId() { return id; }
    public void setId(@Nullable String id) { this.id = id; }
    public List<CategoryConfig> getCategories() { return categories; }
    public void setCategories(List<CategoryConfig> categories) { this.categories = categories; }
    public List<ItemConditionConfig> getItemConditions() { return itemConditions; }
    public void setItemConditions(List<ItemConditionConfig> itemConditions) { this.itemConditions = itemConditions; }
    public List<ReportReasonConfig> getReportReasons() { return reportReasons; }
    public void setReportReasons(List<ReportReasonConfig> reportReasons) { this.reportReasons = reportReasons; }
    public List<PaymentMethodConfig> getSupportedPaymentMethods() { return supportedPaymentMethods; }
    public void setSupportedPaymentMethods(List<PaymentMethodConfig> supportedPaymentMethods) { this.supportedPaymentMethods = supportedPaymentMethods; }
    public List<Integer> getLocationSearchRadiusOptions() { return locationSearchRadiusOptions; }
    public void setLocationSearchRadiusOptions(List<Integer> locationSearchRadiusOptions) { this.locationSearchRadiusOptions = locationSearchRadiusOptions; }
    public int getMaxImageUploadPerItem() { return maxImageUploadPerItem; }
    public void setMaxImageUploadPerItem(int maxImageUploadPerItem) { this.maxImageUploadPerItem = maxImageUploadPerItem; }
    public String getMinAppVersionRequired() { return minAppVersionRequired; }
    public void setMinAppVersionRequired(String minAppVersionRequired) { this.minAppVersionRequired = minAppVersionRequired; }
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
    @Nullable
    public String getMaintenanceMessage() { return maintenanceMessage; }
    public void setMaintenanceMessage(@Nullable String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }
    public CurrencyConfig getCurrency() { return currency; }
    public void setCurrency(CurrencyConfig currency) { this.currency = currency; }
    public ContactSupportConfig getContactSupport() { return contactSupport; }
    public void setContactSupport(ContactSupportConfig contactSupport) { this.contactSupport = contactSupport; }
    @Nullable
    public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }
    public void setPrivacyPolicyUrl(@Nullable String privacyPolicyUrl) { this.privacyPolicyUrl = privacyPolicyUrl; }
    @Nullable
    public String getTermsAndConditionsUrl() { return termsAndConditionsUrl; }
    public void setTermsAndConditionsUrl(@Nullable String termsAndConditionsUrl) { this.termsAndConditionsUrl = termsAndConditionsUrl; }
    public Map<String, List<String>> getSuggestedTags() { return suggestedTags; }
    public void setSuggestedTags(Map<String, List<String>> suggestedTags) { this.suggestedTags = suggestedTags; }
    @Nullable
    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(@Nullable Timestamp lastUpdated) { this.lastUpdated = lastUpdated; }
}