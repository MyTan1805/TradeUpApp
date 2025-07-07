// File: src/main/java/com/example/tradeup/core/di/AppModule.java
package com.example.tradeup.core.di;

import android.content.Context;
import com.example.tradeup.core.utils.SessionManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import com.example.tradeup.data.network.StripeApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Named;
/**
 * Module này chỉ cung cấp các dependency cấp ứng dụng, không thuộc về một
 * nhóm cụ thể nào như Firebase hay Network.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    private static final String STRIPE_BACKEND_URL = "http://10.0.2.2:4242/";

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    @Named("StripeClient")
    public OkHttpClient provideStripeOkHttpClient() { // Đổi tên hàm cho rõ ràng
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    @Named("StripeRetrofit")
    public Retrofit provideStripeRetrofit(@Named("StripeClient") OkHttpClient okHttpClient) { // Yêu cầu client có nhãn "StripeClient"
        return new Retrofit.Builder()
                .baseUrl(STRIPE_BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    public StripeApiService provideStripeApiService(@Named("StripeRetrofit") Retrofit retrofit) { // Yêu cầu Retrofit có nhãn "StripeRetrofit"
        return retrofit.create(StripeApiService.class);
    }

}