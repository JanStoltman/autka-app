package com.yggdralisk.autkaapp.main

import com.yggdralisk.autkaapp.data.network.ApiHelper
import com.yggdralisk.autkaapp.data.network.AppApiHelper
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class MainPresenterModule {
    @Provides
    fun provideMainPresenter(view: MainContract.View, mainRepository: MainRepository): MainContract.Presenter {
        return MainPresenter(view, mainRepository)
    }


    @Provides
    fun provideMainRepository(apiHelper: ApiHelper): MainRepository{
        return MainRepository(apiHelper)
    }
}