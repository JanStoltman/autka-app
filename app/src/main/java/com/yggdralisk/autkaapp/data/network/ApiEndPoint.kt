package com.yggdralisk.autkaapp.data.network

class ApiEndPoint{
    companion object {
        const val BASE_URL = "https://vast-refuge-38856.herokuapp.com"

        val VEHICLES_ENDPOINT = "$BASE_URL/vehicles"
        val CLOSEST_VEHICLES_ENDPOINT = "$BASE_URL/vehicles/closest"
    }
}