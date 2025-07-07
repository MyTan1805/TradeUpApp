// File: src/main/java/com/example/tradeup/core/di/NetworkModule.java
package com.example.tradeup.core.di;

import com.example.tradeup.data.network.NominatimApiService;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request; // << THÊM IMPORT NÀY >>
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        // Logging Interceptor giúp chúng ta xem chi tiết request/response trong Logcat
        // Rất hữu ích để gỡ lỗi.
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                // ======================================================================
                // === BƯỚC SỬA LỖI QUAN TRỌNG NHẤT LÀ ĐÂY ===
                // Thêm một interceptor để tự động chèn User-Agent vào mỗi request.
                // ======================================================================
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            // Thay "your.email@example.com" bằng một email liên hệ thực tế
                            .header("User-Agent", "TradeUpApp/1.0 (contact@tradeup-project.com)")
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();
    }

    // Phương thức này cung cấp instance của Retrofit.
    // Giờ nó sẽ sử dụng OkHttpClient đã được cấu hình đúng ở trên.
    @Provides
    @Singleton
    public Retrofit provideNominatimRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(okHttpClient) // <-- Dùng client đã có User-Agent
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Phương thức này không cần thay đổi.
    @Provides
    @Singleton
    public NominatimApiService provideNominatimApiService(Retrofit retrofit) {
        return retrofit.create(NominatimApiService.class);
    }
}