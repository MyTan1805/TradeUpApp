// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    id("androidx.navigation.safeargs") version "2.7.7" apply false
//    alias(libs.plugins.androidx.navigation.safeargs.kotlin) apply false
}
