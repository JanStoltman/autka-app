package com.yggdralisk.autkaapp.main

import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModule {
    @Binds
    abstract fun provideMainView(activity: MainActivity): MainContract.View
}