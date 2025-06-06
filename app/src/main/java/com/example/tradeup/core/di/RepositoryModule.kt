package com.example.tradeup.core.di

// Import các interface Repository
import com.example.tradeup.data.repository.AppConfigRepository
import com.example.tradeup.data.repository.AuthRepository
import com.example.tradeup.data.repository.ChatRepository
import com.example.tradeup.data.repository.ItemRepository
import com.example.tradeup.data.repository.OfferRepository
import com.example.tradeup.data.repository.RatingRepository
import com.example.tradeup.data.repository.ReportRepository
import com.example.tradeup.data.repository.TransactionRepository
import com.example.tradeup.data.repository.UserRepository
import com.example.tradeup.data.repository.UserSavedItemsRepository

// Import các implementation của Repository
import com.example.tradeup.data.repository.AppConfigRepositoryImpl
import com.example.tradeup.data.repository.AuthRepositoryImpl
import com.example.tradeup.data.repository.ChatRepositoryImpl
import com.example.tradeup.data.repository.ItemRepositoryImpl
import com.example.tradeup.data.repository.OfferRepositoryImpl
import com.example.tradeup.data.repository.RatingRepositoryImpl
import com.example.tradeup.data.repository.ReportRepositoryImpl
import com.example.tradeup.data.repository.TransactionRepositoryImpl
import com.example.tradeup.data.repository.UserRepositoryImpl
import com.example.tradeup.data.repository.UserSavedItemsRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        itemRepositoryImpl: ItemRepositoryImpl
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindAppConfigRepository(
        appConfigRepositoryImpl: AppConfigRepositoryImpl
    ): AppConfigRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository


    @Binds
    @Singleton
    abstract fun bindOfferRepository(
        offerRepositoryImpl: OfferRepositoryImpl
    ): OfferRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindRatingRepository(
        ratingRepositoryImpl: RatingRepositoryImpl
    ): RatingRepository

    @Binds
    @Singleton
    abstract fun bindUserSavedItemsRepository(
        userSavedItemsRepositoryImpl: UserSavedItemsRepositoryImpl
    ): UserSavedItemsRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

}