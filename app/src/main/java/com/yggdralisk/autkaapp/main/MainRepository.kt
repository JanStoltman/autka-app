package com.yggdralisk.autkaapp.main

import com.yggdralisk.autkaapp.common.extension.isTerminated
import com.yggdralisk.autkaapp.common.extension.removeAndAddAll
import com.yggdralisk.autkaapp.data.network.ApiHelper
import com.yggdralisk.autkaapp.data.network.model.CarModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository
@Inject constructor(private val apiHelper: ApiHelper) {
    private val carsCache = arrayListOf<CarModel>()
    private var carsPublishSubject = PublishSubject.create<Pair<List<CarModel>?, Throwable?>>()
    private val carsDisposable = CompositeDisposable()

    fun getCarsObservableAndStartPolling(): Observable<Pair<List<CarModel>?, Throwable?>> {
        if (carsPublishSubject.isTerminated()) {
            recreateCarsPublishSubject()
        }
        pushCache()

        startCarPolling()
        return carsPublishSubject.cache()
    }

    private fun pushCache() {
        if (carsCache.isNotEmpty()) {
            carsPublishSubject.onNext(Pair(carsCache, null))
        }
    }

    private fun recreateCarsPublishSubject() {
        carsDisposable.clear()
        carsPublishSubject = PublishSubject.create<Pair<List<CarModel>?, Throwable?>>()
    }

    private fun startCarPolling() = apiHelper
            .getCars()
            .repeatWhen { single -> single.delay(45, TimeUnit.SECONDS) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleCars, ::handleFetchError)
            .addTo(carsDisposable)

    fun refreshCars() = apiHelper
            .getCars()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleCars, ::handleFetchError)
            .addTo(carsDisposable)

    private fun handleFetchError(throwable: Throwable) {
        Timber.e(throwable)
        carsPublishSubject.onNext(Pair(null, throwable))
    }

    private fun handleCars(cars: List<CarModel>) {
        carsCache.removeAndAddAll(cars)

        if (carsPublishSubject.isTerminated()) {
            recreateCarsPublishSubject()
        }

        carsPublishSubject.onNext(Pair(cars, null))
    }

    fun stopFetch() {
        carsDisposable.clear()
        carsPublishSubject.onComplete()
        carsCache.clear()
    }
}