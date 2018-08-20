package com.yggdralisk.autkaapp.main

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
        fun showCatView()
        fun hideFilterView()
        fun loadCatView(imageUrl: String)
        fun hideCatView()
        fun showFilterView()
        fun isFilterViewVisible(): Boolean
        fun isCatViewVisible(): Boolean
        fun isVozillaSwitchChecked(): Boolean
        fun isTraficarSwitchChecked(): Boolean
    }

    interface Presenter : BasePresenterInterface {
        fun onMapReady()
        fun onLocationPermissionNotGranted()
        fun onLocationPermissionGranted()
        fun onLocationPermissionDenied()
        fun onFilterButtonClick(locationOnScreen: IntArray)
        fun onRefreshButtonClick()
        fun onCatButtonClick(locationOnScreen: IntArray)
        fun onVozillaSelectionChanged(checked: Boolean)
        fun onTraficarSelectionChanged(checked: Boolean)
    }
}