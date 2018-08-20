package com.yggdralisk.autkaapp.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule {
    @Provides
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }
}