package com.yggdralisk.autkaapp.data.network

import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.CatImageResponse
import io.reactivex.Single

interface ApiHelper {
    fun getApiHeader(): ApiHeader
    fun getCars(): Single<List<CarModel>>
    fun getClosestCar(): Single<CarModel>
    fun getCatPic(): Single<CatImageResponse>
}