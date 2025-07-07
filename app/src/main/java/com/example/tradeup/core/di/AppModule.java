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

/**
 * Module này chỉ cung cấp các dependency cấp ứng dụng, không thuộc về một
 * nhóm cụ thể nào như Firebase hay Network.
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

}