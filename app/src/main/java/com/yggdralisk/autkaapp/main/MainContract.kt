package com.yggdralisk.autkaapp.main

import com.google.android.gms.maps.model.Marker
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.mvp.BasePresenterInterface

interface MainContract {
    interface View {
        fun fetchAndDisplayLatestLocation()
        fun checkLocationPermission(): Boolean
        fun requestLocationPermission()
        fun showError(message: String?)
        fun displayCars(cars: List<CarModel>?)
        fun animateHoveringToolbarUp()
        fun animateHoveringToolbarDown()
        fun getUtilDialogHeight(): Float
        fun getScreenHeight(): Int
        fun animateUtilViewHeightUp()
        fun animateUtilViewHeightDown()
        fun isVozillaSwitchChecked(): Boolean
        fun isTraficarSwitchChecked(): Boolean
        fun showDetailsView(car: CarModel)
        fun hideBottomSheet()
        fun carDetailsLayoutBehaviorIsHidden(): Boolean
        fun isHoveringToolbarDown(): Boolean
    }

    interface Presenter : BasePresenterInterface {
        fun onMapReady()
        fun onLocationPermissionNotGranted()
        fun onLocationPermissionGranted()
        fun onLocationPermissionDenied()
        fun onFilterButtonClick(locationOnScreen: IntArray)
        fun onRefreshButtonClick()
        fun onLocationButtonClick()
        fun onVozillaSelectionChanged(checked: Boolean)
        fun onTraficarSelectionChanged(checked: Boolean)
        fun onMarkerClick(marker: Marker?): Boolean
        fun onBackPressed(): Boolean
        fun onMapClick()
    }
}