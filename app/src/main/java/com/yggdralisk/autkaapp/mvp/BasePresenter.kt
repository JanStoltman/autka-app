package com.yggdralisk.autkaapp.mvp

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V>(val view: V): BasePresenterInterface{
    protected val subscriptions = CompositeDisposable()

    override fun onStart(){

    }

    override fun onDestroy(){
        subscriptions.clear()
    }
}