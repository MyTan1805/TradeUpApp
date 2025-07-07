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
    implementation(libs.hilt.android)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui)
    implementation(libs.google.material)
    kapt(libs.hilt.compiler)

    implementation(libs.preference)

    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.1")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("ch.hsr:geohash:1.4.0")
    implementation("io.grpc:grpc-okhttp:1.58.0")

    implementation("com.firebase:geofire-android-common:3.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    implementation("com.stripe:stripe-android:20.39.0") // Luôn kiểm tra phiên bản mới nhất

    // Thư viện Retrofit để gọi API đến server
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation("com.google.firebase:firebase-messaging-ktx")

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

kapt {
    correctErrorTypes = true
}