package com.yggdralisk.autkaapp.data.network

class ApiEndPoint{
    companion object {
        const val BASE_URL = "http://80.211.47.70"

        val VEHICLES_ENDPOINT = "$BASE_URL/vehicles"
        val CLOSEST_VEHICLES_ENDPOINT = "$BASE_URL/vehicles/closest"
        val RANDOM_CAT_PIC = "https://aws.random.cat/meow"
    }
}