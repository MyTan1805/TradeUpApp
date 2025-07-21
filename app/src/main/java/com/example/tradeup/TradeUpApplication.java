// File: com/example/tradeup/TradeUpApplication.java

package com.example.tradeup;

import android.app.Application;
import androidx.emoji2.text.EmojiCompat;
import com.google.firebase.FirebaseApp;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class TradeUpApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EmojiCompat.init(this);
        EmojiManager.install(new GoogleEmojiProvider());
        FirebaseApp.initializeApp(this);
    }
}