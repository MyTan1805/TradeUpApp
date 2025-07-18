package com.example.tradeup;

import android.app.Application;

import androidx.emoji2.text.EmojiCompat;

import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;

import dagger.hilt.android.HiltAndroidApp;

import androidx.emoji2.text.EmojiCompat;

@HiltAndroidApp
public class TradeUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        EmojiCompat.init(this);

    }
}