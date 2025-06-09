package com.example.tradeup.core.di

import android.content.Context
import com.example.tradeup.core.utils.SessionManager
import com.example.tradeup.data.source.remote.FirebaseItemSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton // Đảm bảo Hilt chỉ tạo một instance duy nhất của SessionManager cho toàn ứng dụng
    @Provides // Đánh dấu hàm này là một provider (nơi cung cấp dependency)
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Singleton
    @Provides
    fun provideFirebaseItemSource(firestore: FirebaseFirestore): FirebaseItemSource { // Cung cấp FirebaseItemSource
        return FirebaseItemSource(firestore)
    }

}