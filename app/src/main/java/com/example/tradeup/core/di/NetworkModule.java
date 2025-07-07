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

import javax.inject.Named;
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/";

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        // Logging Interceptor giúp chúng ta xem chi tiết request/response trong Logcat
        // Rất hữu ích để gỡ lỗi.
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    @Named("NominatimClient")
    public OkHttpClient provideNominatimOkHttpClient() { // Đổi tên hàm cho rõ ràng
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    // Phương thức này cung cấp instance của Retrofit.
    // Giờ nó sẽ sử dụng OkHttpClient đã được cấu hình đúng ở trên.
    @Provides
    @Singleton
    @Named("NominatimRetrofit")
    public Retrofit provideNominatimRetrofit(@Named("NominatimClient") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(NOMINATIM_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    // Phương thức này không cần thay đổi.
    @Provides
    @Singleton
    public NominatimApiService provideNominatimApiService(@Named("NominatimRetrofit") Retrofit retrofit) {
        return retrofit.create(NominatimApiService.class);
    }
}