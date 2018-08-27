package com.yggdralisk.autkaapp.main

import com.google.android.gms.maps.model.Marker
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.Owner
import com.yggdralisk.autkaapp.mvp.BasePresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import timber.log.Timber

class MainPresenter(view: MainContract.View,
                    private val mainRepository: MainRepository) : BasePresenter<MainContract.View>(view), MainContract.Presenter {
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
        mainRepository.fetchCars()
                .map { cl ->
                    val owners = getActiveOwners()
                    cl.filter { c ->
                        c.Owner in owners
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.displayCars(it)
                }, {
                    Timber.e(it)
                    view.showError(it.message)
                }).addTo(subscriptions)
    }

    private fun getActiveOwners(): Set<String> {
        val s = hashSetOf<String>()

        if (view.isVozillaSwitchChecked()) {
            s.add(Owner.VOZILLA.ownerName)
        }

        if (view.isTraficarSwitchChecked()) {
            s.add(Owner.TRAFICAR.ownerName)
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

    override fun onRefreshButtonClick() {
        doAsync {
            mainRepository.refreshCars()
        }
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