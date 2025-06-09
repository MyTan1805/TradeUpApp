package com.example.tradeup // Hoặc package đúng của bạn

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp // Nếu bạn dùng Hilt

@HiltAndroidApp
class TradeUpApplication : Application() {

    companion object {
        private const val TAG = "TradeUpApplication"
    }

    override fun onCreate() {
        Log.e("MY_APP_DEBUG", "TradeUpApplication onCreate START") // << THÊM LOG
        super.onCreate()
        Log.d(TAG, "TradeUpApplication super.onCreate() called.")


        Log.e("MY_APP_DEBUG", "TradeUpApplication onCreate END") // << THÊM LOG
    }
}