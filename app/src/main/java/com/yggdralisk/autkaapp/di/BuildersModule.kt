package com.yggdralisk.autkaapp.di

import com.yggdralisk.autkaapp.main.MainActivity
import com.yggdralisk.autkaapp.main.MainPresenterModule
import com.yggdralisk.autkaapp.main.MainViewModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {
    @ContributesAndroidInjector(modules = [MainPresenterModule::class, MainViewModule::class, ApiModule::class])
    abstract fun mainActivity(): MainActivity
}