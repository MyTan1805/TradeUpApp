// Đặt ở trên cùng của file
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin") // Sử dụng phiên bản Kotlin của safe-args
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { fis -> load(fis) }
    }
}
android {
    namespace = "com.example.tradeup"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tradeup"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        fun getProperty(key: String, defaultValue: String = ""): String {
            return localProperties.getProperty(key, defaultValue)
        }

        resValue(
            "string", // Loại tài nguyên là string
            "maps_api_key", // Tên của string resource (viết thường)
            localProperties.getProperty("MAPS_API_KEY", "DEFAULT_API_KEY_IF_NOT_FOUND") // Lấy giá trị từ local.properties
        )

        resValue("string", "maps_api_key", localProperties.getProperty("MAPS_API_KEY", "DEFAULT_API_KEY_IF_NOT_FOUND"))
        resValue("string", "cloudinary_cloud_name", localProperties.getProperty("CLOUDINARY_CLOUD_NAME", "DEFAULT_CLOUD_NAME"))
        resValue("string", "cloudinary_api_key", localProperties.getProperty("CLOUDINARY_API_KEY", "DEFAULT_API_KEY"))
        resValue("string", "cloudinary_upload_preset", localProperties.getProperty("CLOUDINARY_UPLOAD_PRESET", "DEFAULT_UPLOAD_PRESET"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}
dependencies {
    // Core & UI
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material) // *** CHỈ DÙNG MỘT ALIAS DUY NHẤT ***
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Có thể nâng lên 33.5.1
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation(libs.firebase.firestore.ktx)
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Coroutines (Vẫn cần cho một số thư viện của Google dù bạn code Java)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    // Google Services
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Có thể nâng lên 21.x
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Networking (Retrofit & OkHttp)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Geo & Location
    implementation("ch.hsr:geohash:1.4.0")
    implementation("com.firebase:geofire-android-common:3.2.0")

    // Payment (Stripe)
    implementation("com.stripe:stripe-android:20.39.0")

    // UI Utilities
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("androidx.emoji2:emoji2:1.4.0")

    // Preference
    implementation(libs.preference)

    // GRPC (cần cho Firestore)
    implementation("io.grpc:grpc-okhttp:1.58.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.vanniktech:emoji-google:0.15.0")
}

kapt {
    correctErrorTypes = true
}