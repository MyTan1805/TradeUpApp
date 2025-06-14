package com.example.tradeup.data.repository;

import androidx.annotation.Nullable; // AndroidX annotation for nullability

import com.example.tradeup.core.utils.Callback;
import com.example.tradeup.data.model.config.AppConfig; // Model AppConfig (Java class)
import com.example.tradeup.data.source.remote.AppConfigSource; // AppConfigSource (Java class)

import javax.inject.Inject;
import javax.inject.Singleton; // Thêm nếu bạn muốn Repository là Singleton (thường là vậy)

@Singleton // Đảm bảo chỉ có một instance của Repository này nếu dùng Hilt
public class AppConfigRepositoryImpl implements AppConfigRepository {

    private final AppConfigSource appConfigSource;

    @Nullable
    private volatile AppConfig cachedConfig = null; // Dùng volatile nếu có thể truy cập từ nhiều thread (an toàn hơn)

    @Inject
    public AppConfigRepositoryImpl(AppConfigSource appConfigSource) {
        this.appConfigSource = appConfigSource;
    }

    @Override
    public void getAppConfig(final Callback<AppConfig> callback) {
        AppConfig localCache = cachedConfig; // Đọc cache một lần để thread-safety
        if (localCache != null) {
            callback.onSuccess(localCache);
            return;
        }

        // Nếu không có trong cache, gọi từ remote source
        appConfigSource.getAppConfig(new Callback<AppConfig>() {
            @Override
            public void onSuccess(AppConfig config) {
                // Lưu kết quả vào cache
                cachedConfig = config;
                // Trả kết quả thành công về cho nơi đã gọi
                callback.onSuccess(config);
            }

            @Override
            public void onFailure(Exception e) {
                // Trả lỗi về cho nơi đã gọi
                callback.onFailure(e);
            }
        });
    }

    // (Tùy chọn) Hàm để xóa cache nếu cần
    public void clearCache() {
        cachedConfig = null;
    }
}