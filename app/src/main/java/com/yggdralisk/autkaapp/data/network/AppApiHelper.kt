package com.yggdralisk.autkaapp.data.network

import com.rx2androidnetworking.Rx2AndroidNetworking
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.CatImageResponse
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppApiHelper
@Inject constructor(private val apiHeader: ApiHeader) : ApiHelper {
    override fun getApiHeader(): ApiHeader {
        return apiHeader
    }

    override fun getCars(): Single<List<CarModel>> {
        return Rx2AndroidNetworking.get(ApiEndPoint.VEHICLES_ENDPOINT)
                .addHeaders(apiHeader)
                .build()
                .getObjectListSingle(CarModel::class.java)
    }

    override fun getClosestCar(): Single<CarModel> {
        return Rx2AndroidNetworking.get(ApiEndPoint.CLOSEST_VEHICLES_ENDPOINT)
                .addHeaders(apiHeader)
                .build()
                .getObjectSingle(CarModel::class.java)
    }

    override fun getCatPic(): Single<CatImageResponse> {
        return Rx2AndroidNetworking.get(ApiEndPoint.RANDOM_CAT_PIC)
                .build()
                .getObjectSingle(CatImageResponse::class.java)
    }
}