package com.example.tradeup.core.di;

import android.content.Context;
import com.example.tradeup.core.utils.SessionManager;
import com.example.tradeup.data.source.remote.FirebaseItemSource;
import com.google.firebase.firestore.FirebaseFirestore;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Singleton
    @Provides

    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    public FirebaseItemSource provideFirebaseItemSource(FirebaseFirestore firestore) {
        return new FirebaseItemSource(firestore);
    }
}