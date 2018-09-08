package com.yggdralisk.autkaapp.data.network.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class CarModel(
        @SerializedName("owner") val owner: String,
        @SerializedName("model") val model: String,
        @SerializedName("plate_number") val plateNumber: String,
        @SerializedName("side_number") val sideNumber: String,
        @SerializedName("range_km") val rangeKm: Int?,
        @SerializedName("fuel_l") val fuel: Int?,
        @SerializedName("lat") val lat: Double,
        @SerializedName("lon") val lon: Double
) {
    fun toLatLng(): LatLng {
        return LatLng(this.lat, this.lon)
    }

    fun isOwnedBy(owner: Owner): Boolean {
        return this.owner.toLowerCase().trim() == owner.ownerName.toLowerCase().trim()
    }

    fun isOwnedByAnyOf(owners: Set<Owner>): Boolean {
        return owners.any { o -> this.isOwnedBy(o) }
    }
}