package com.example.tradeup.core.di;

import android.content.Context;
import com.example.tradeup.core.utils.SessionManager;
import com.example.tradeup.data.network.StripeApiService;
import com.example.tradeup.data.network.NotificationApiService;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    private static final String BASE_URL = "http://10.0.2.2:4242/"; // Chung cho Stripe và Notification

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    @Named("AppClient")
    public OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    @Named("AppRetrofit")
    public Retrofit provideRetrofit(@Named("AppClient") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    public StripeApiService provideStripeApiService(@Named("AppRetrofit") Retrofit retrofit) {
        return retrofit.create(StripeApiService.class);
    }

    @Provides
    @Singleton
    public NotificationApiService provideNotificationApiService(@Named("AppRetrofit") Retrofit retrofit) {
        return retrofit.create(NotificationApiService.class);
    }
}