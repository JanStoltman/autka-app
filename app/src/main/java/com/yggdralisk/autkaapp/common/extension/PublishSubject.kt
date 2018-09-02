package com.yggdralisk.autkaapp.common.extension

import io.reactivex.subjects.PublishSubject

fun <T> PublishSubject<T>.safeOnError(throwable: Throwable) {
    if(this.hasThrowable() || this.hasComplete()){
        return
    }else{
        this.onError(throwable)
    }
}

fun <T> PublishSubject<T>.isTerminated(): Boolean = this.hasThrowable() || this.hasComplete()