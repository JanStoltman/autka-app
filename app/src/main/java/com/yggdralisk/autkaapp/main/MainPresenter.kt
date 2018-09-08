package com.yggdralisk.autkaapp.main

import com.google.android.gms.maps.model.Marker
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.Owner
import com.yggdralisk.autkaapp.mvp.BasePresenter
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class MainPresenter(view: MainContract.View,
                    private val mainRepository: MainRepository) : BasePresenter<MainContract.View>(view), MainContract.Presenter {
    override fun onDestroy() {
        super.onDestroy()
        mainRepository.stopFetch()
    }

    override fun onVozillaSelectionChanged(checked: Boolean) {
        fetchCars()
    }

    override fun onTraficarSelectionChanged(checked: Boolean) {
        fetchCars()
    }

    override fun onLocationPermissionGranted() {
        view.fetchAndDisplayLatestLocation()
    }

    override fun onLocationPermissionDenied() {
        //No-op for now
    }

    override fun onLocationPermissionNotGranted() {
        view.requestLocationPermission()
    }

    override fun onMapReady() {
        view.fetchAndDisplayLatestLocation()
        fetchCars()
    }

    private fun fetchCars() {
        mainRepository.getCarsObservableAndStartPolling()
                .map { cl ->
                    val owners = getActiveOwners()
                    Pair(cl.first?.filter { c -> c.isOwnedByAnyOf(owners) }, cl.second)
                }
                .subscribe(::handleCarsResult, ::handleFetchError)
                .addTo(subscriptions)
    }

    private fun handleCarsResult(resultPair: Pair<List<CarModel>?, Throwable?>) {
        if (resultPair.second == null) {
            view.displayCars(resultPair.first)
        } else {
            handleFetchError(resultPair.second)
        }
    }

    private fun handleFetchError(throwable: Throwable?) {
        Timber.e(throwable)
        view.showError()
    }

    override fun onRefreshButtonClick() {
        mainRepository.refreshCars()
    }

    private fun getActiveOwners(): Set<Owner> {
        val s = hashSetOf<Owner>()

        if (view.isVozillaSwitchChecked()) {
            s.add(Owner.VOZILLA)
        }

        if (view.isTraficarSwitchChecked()) {
            s.add(Owner.TRAFICAR)
        }

        return s
    }

    override fun onFilterButtonClick(locationOnScreen: IntArray) =
            if (view.isHoveringToolbarDown()) {
                showUtilView()
            } else {
                hideUtilView()
            }

    override fun onLocationButtonClick() {
        view.fetchAndDisplayLatestLocation()
    }

    private fun hideUtilView() {
        view.animateHoveringToolbarDown()
        view.animateUtilViewHeightDown()
    }

    private fun showUtilView() {
        view.animateHoveringToolbarUp()
        view.animateUtilViewHeightUp()
    }

    override fun onMarkerClick(marker: Marker?): Boolean =
            if (marker == null || marker.tag !is CarModel) {
                false
            } else {
                hideUtilView()
                view.showDetailsView(marker.tag as CarModel)
                true
            }

    override fun onBackPressed(): Boolean =
            when {
                view.isHoveringToolbarDown().not() -> {
                    hideUtilView()
                    true
                }
                view.carDetailsLayoutBehaviorIsHidden().not() -> {
                    view.hideBottomSheet()
                    true
                }
                else -> false
            }

    override fun onMapClick() {
        view.hideBottomSheet()
        hideUtilView()
    }
}