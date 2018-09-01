package com.yggdralisk.autkaapp.common.extension

import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

const val DEFAULT_ZOOM_LEVEL = 15f

fun GoogleMap.zoomToLocation(location: Location, zoomLevel: Float? = DEFAULT_ZOOM_LEVEL) =
        zoomToCoordinates(latitude = location.latitude, longitude = location.longitude, zoomLevel = zoomLevel)

fun GoogleMap.zoomToLatLng(latLng: LatLng, zoomLevel: Float? = DEFAULT_ZOOM_LEVEL) =
        zoomToCoordinates(latitude = latLng.latitude, longitude = latLng.longitude, zoomLevel = zoomLevel)

fun GoogleMap.zoomToLatLngFromTop(latLng: LatLng, zoomLevel: Float? = DEFAULT_ZOOM_LEVEL) =
        zoomToCoordinates(latitude = latLng.latitude, longitude = latLng.longitude,
                zoomLevel = if (this.cameraPosition.zoom < DEFAULT_ZOOM_LEVEL) zoomLevel else this.cameraPosition.zoom)

fun GoogleMap.zoomToCoordinates(latitude: Double, longitude: Double, zoomLevel: Float? = DEFAULT_ZOOM_LEVEL) {
    val cameraUpdate = CameraUpdateFactory
            .newLatLngZoom(LatLng(latitude, longitude), zoomLevel ?: this.cameraPosition.zoom)
    this.animateCamera(cameraUpdate)
}