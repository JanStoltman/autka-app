package com.yggdralisk.autkaapp.main

import com.yggdralisk.autkaapp.common.extension.removeAndAddAll
import com.yggdralisk.autkaapp.data.network.ApiHelper
import com.yggdralisk.autkaapp.data.network.model.CarModel
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository
@Inject constructor(private val apiHelper: ApiHelper) {
    private val carsCache = arrayListOf<CarModel>()
    private var carsEmitter: Emitter<List<CarModel>>? = null
    private val carsDisposable = CompositeDisposable()

    fun fetchCars(): Observable<List<CarModel>> =
            Observable.create { emitter ->
                carsEmitter = emitter

                if (carsCache.isNotEmpty()) {
                    carsEmitter?.onNext(carsCache)
                }

                startCarPolling()
            }

    private fun startCarPolling() = apiHelper
            .getCars()
            .repeatWhen { single -> single.delay(30, TimeUnit.SECONDS) }
            .subscribe(::handleCars, ::handleFetchError)
            .addTo(carsDisposable)

    fun refreshCars() = apiHelper
            .getCars()
            .subscribe(::handleCars, ::handleFetchError)
            .addTo(carsDisposable)

    private fun handleFetchError(throwable: Throwable) {
        Timber.e(throwable)
        carsEmitter?.onError(throwable)
    }

    private fun handleCars(cars: List<CarModel>) {
        carsCache.removeAndAddAll(cars)
        carsEmitter?.onNext(carsCache)
    }
}