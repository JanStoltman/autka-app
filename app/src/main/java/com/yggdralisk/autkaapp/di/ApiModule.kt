package com.yggdralisk.autkaapp.di

import com.yggdralisk.autkaapp.data.network.ApiHelper
import com.yggdralisk.autkaapp.data.network.AppApiHelper
import dagger.Module
import dagger.Provides

@Module
class ApiModule {
    @Provides
    fun provideApiHelper(apiHelper: AppApiHelper): ApiHelper {
        return apiHelper
    }
}