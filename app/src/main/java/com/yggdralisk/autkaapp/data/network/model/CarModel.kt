package com.yggdralisk.autkaapp.data.network.model

import com.google.android.gms.maps.model.LatLng

data class CarModel(
        val Owner: String,
        val Model: String,
        val PlateNumber: String,
        val SideNumber: String,
        val RangeKm: Int,
        val Fuel: Int,
        val Latitude: Double,
        val Longitude: Double
) {
    fun toLatLng(): LatLng {
        return LatLng(this.Latitude, this.Longitude)
    }
}