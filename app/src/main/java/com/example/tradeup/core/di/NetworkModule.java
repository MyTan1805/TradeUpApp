// File: src/main/java/com/example/tradeup/di/NetworkModule.java
package com.example.tradeup.core.di;

import com.example.tradeup.data.network.ApiService;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "https://your-api.com/api/"; // << THAY THẾ BASE URL

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        // Cấu hình để chỉ log khi ở chế độ debug
        // if (BuildConfig.DEBUG) {
        //     return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        // } else {
        //     return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE);
        // }
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}