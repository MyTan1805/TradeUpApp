package com.example.tradeup.core.di;

// Import các interface Repository (phiên bản Java)
import com.example.tradeup.data.repository.AppConfigRepository;
import com.example.tradeup.data.repository.AuthRepository;
import com.example.tradeup.data.repository.ChatRepository;
import com.example.tradeup.data.repository.ItemRepository;
import com.example.tradeup.data.repository.OfferRepository;
import com.example.tradeup.data.repository.RatingRepository;
import com.example.tradeup.data.repository.ReportRepository;
import com.example.tradeup.data.repository.TransactionRepository;
import com.example.tradeup.data.repository.UserRepository;
import com.example.tradeup.data.repository.UserSavedItemsRepository;

// Import các implementation của Repository (phiên bản Java)
import com.example.tradeup.data.repository.AppConfigRepositoryImpl;
import com.example.tradeup.data.repository.AuthRepositoryImpl;
import com.example.tradeup.data.repository.ChatRepositoryImpl;
import com.example.tradeup.data.repository.ItemRepositoryImpl;
import com.example.tradeup.data.repository.OfferRepositoryImpl;
import com.example.tradeup.data.repository.RatingRepositoryImpl;
import com.example.tradeup.data.repository.ReportRepositoryImpl;
import com.example.tradeup.data.repository.TransactionRepositoryImpl;
import com.example.tradeup.data.repository.UserRepositoryImpl;
import com.example.tradeup.data.repository.UserSavedItemsRepositoryImpl;

import javax.inject.Singleton;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import com.example.tradeup.data.repository.LocationRepository;
import com.example.tradeup.data.repository.LocationRepositoryImpl;

import com.example.tradeup.data.repository.NotificationRepository;
import com.example.tradeup.data.repository.NotificationRepositoryImpl;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule { // Giữ nguyên abstract class cho @Binds

    @Binds
    @Singleton
    public abstract AuthRepository bindAuthRepository(AuthRepositoryImpl authRepositoryImpl);

    @Binds
    @Singleton
    public abstract UserRepository bindUserRepository(UserRepositoryImpl userRepositoryImpl);

    @Binds
    @Singleton
    public abstract ItemRepository bindItemRepository(ItemRepositoryImpl itemRepositoryImpl);

    @Binds
    @Singleton
    public abstract AppConfigRepository bindAppConfigRepository(AppConfigRepositoryImpl appConfigRepositoryImpl);

    @Binds
    @Singleton
    public abstract ChatRepository bindChatRepository(ChatRepositoryImpl chatRepositoryImpl);

    @Binds
    @Singleton
    public abstract OfferRepository bindOfferRepository(OfferRepositoryImpl offerRepositoryImpl);

    @Binds
    @Singleton
    public abstract TransactionRepository bindTransactionRepository(TransactionRepositoryImpl transactionRepositoryImpl);

    @Binds
    @Singleton
    public abstract RatingRepository bindRatingRepository(RatingRepositoryImpl ratingRepositoryImpl);

    @Binds
    @Singleton
    public abstract UserSavedItemsRepository bindUserSavedItemsRepository(UserSavedItemsRepositoryImpl userSavedItemsRepositoryImpl);

    @Binds
    @Singleton
    public abstract ReportRepository bindReportRepository(ReportRepositoryImpl reportRepositoryImpl);

    @Binds
    @Singleton
    public abstract LocationRepository bindLocationRepository(LocationRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract NotificationRepository bindNotificationRepository(NotificationRepositoryImpl impl);
}