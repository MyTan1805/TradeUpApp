package com.example.tradeup; // Hoặc package đúng của bạn

import android.app.Application;
import android.util.Log;
import dagger.hilt.android.HiltAndroidApp; // Import Hilt nếu bạn dùng

@HiltAndroidApp // Giữ lại annotation này nếu bạn dùng Hilt
public class TradeUpApplication extends Application {

    // Tương đương với companion object private const val TAG
    private static final String TAG = "TradeUpApplication";

    @Override
    public void onCreate() {
        Log.e("MY_APP_DEBUG", "TradeUpApplication onCreate START"); // Log của bạn
        super.onCreate();
        Log.d(TAG, "TradeUpApplication super.onCreate() called.");

        // Khởi tạo các thư viện hoặc tác vụ toàn cục khác ở đây nếu cần
        // Ví dụ:
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(new Timber.DebugTree());
        //     Log.d(TAG, "Timber initialized for DEBUG build.");
        // }
        // initializeFirebase(); // Một hàm tùy chỉnh nếu bạn cần khởi tạo Firebase thêm
        // initializeAnalytics();

        Log.e("MY_APP_DEBUG", "TradeUpApplication onCreate END"); // Log của bạn
    }

    // Bạn có thể thêm các phương thức tùy chỉnh khác ở đây nếu cần
    // private void initializeFirebase() {
    //     // Logic khởi tạo Firebase tùy chỉnh (nếu mặc định không đủ)
    //     Log.d(TAG, "Custom Firebase initialization (if any).");
    // }

    // private void initializeAnalytics() {
    //     // Logic khởi tạo thư viện analytics
    //     Log.d(TAG, "Analytics initialized.");
    // }
}