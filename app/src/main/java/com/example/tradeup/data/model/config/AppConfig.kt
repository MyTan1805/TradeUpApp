package com.example.tradeup.data.model.config

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// --- Nested Data Classes for AppConfig ---

data class CategoryConfig(
    val id: String = "",
    val name: String = "",
    val iconUrl: String? = null,
    val subcategories: List<SubcategoryConfig> = emptyList()
)

data class SubcategoryConfig(
    val id: String = "",
    val name: String = ""
)

data class ItemConditionConfig(
    val id: String = "",
    val name: String = ""
)

data class ReportReasonConfig(
    val id: String = "",
    val name: String = ""
)

data class PaymentMethodConfig(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val iconUrl: String? = null,
    val type: String = "", // "offline", "online_gateway"
    val isActive: Boolean = false
)

data class CurrencyConfig(
    val code: String = "", // "VND"
    val name: String = "", // "Việt Nam Đồng"
    val symbol: String = ""  // "₫"
)

data class ContactSupportConfig(
    val email: String? = null,
    val phone: String? = null,
    val faqUrl: String? = null
)

// --- Main AppConfig Data Class ---

data class AppConfig(
    @DocumentId val id: String? = "global", // Should always be "global"
    val categories: List<CategoryConfig> = emptyList(),
    val itemConditions: List<ItemConditionConfig> = emptyList(),
    val reportReasons: List<ReportReasonConfig> = emptyList(),
    val supportedPaymentMethods: List<PaymentMethodConfig> = emptyList(),
    val locationSearchRadiusOptions: List<Int> = listOf(5, 10, 25, 50, 100),
    val maxImageUploadPerItem: Int = 10,
    val minAppVersionRequired: String = "1.0.0",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String? = null,
    val currency: CurrencyConfig = CurrencyConfig(),
    val contactSupport: ContactSupportConfig = ContactSupportConfig(),
    val privacyPolicyUrl: String? = null,
    val termsAndConditionsUrl: String? = null,
    val suggestedTags: Map<String, List<String>> = emptyMap(), // Key: subcategoryId
    @ServerTimestamp val lastUpdated: Timestamp? = null
)