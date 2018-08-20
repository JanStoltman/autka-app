package com.yggdralisk.autkaapp.data.network.model

data class CarModel(
        val Owner: String,
        val Model: String,
        val PlateNumber: String,
        val SideNumber: String,
        val RangeKm: Int,
        val Fuel: Int,
        val Latitude: Double,
        val Longitude: Double
)