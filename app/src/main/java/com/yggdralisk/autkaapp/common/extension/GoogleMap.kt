package com.yggdralisk.autkaapp.common.extension

import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

const val DEFAULT_ZOOM_LEVEL = 15f

fun GoogleMap.zoomToLocation(location: Location) =
        zoomToCoordinates(latitude = location.latitude, longitude = location.longitude)

fun GoogleMap.zoomToLatLng(latLng: LatLng) =
        zoomToCoordinates(latitude = latLng.latitude, longitude = latLng.longitude)

fun GoogleMap.zoomToCoordinates(latitude: Double, longitude: Double) {
    val cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(LatLng(latitude, longitude), DEFAULT_ZOOM_LEVEL)
    this.animateCamera(cameraUpdate)
}