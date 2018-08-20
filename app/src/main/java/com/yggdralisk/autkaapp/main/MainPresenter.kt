package com.yggdralisk.autkaapp.main

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

    override fun onStart() {
        super.onStart()
        fetchAndLoadCatImage()
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

    override fun onFilterButtonClick(locationOnScreen: IntArray) {
        val chosenAgain = view.isFilterViewVisible()

        view.hideCatView()
        view.showFilterView()

        if (isHoveringToolbarDown(locationOnScreen)) {
            showUtilView()
        } else if (chosenAgain) {
            hideUtilView()
        }
    }

    override fun onCatButtonClick(locationOnScreen: IntArray) {
        val chosenAgain = view.isCatViewVisible()

        showCatImage()
        fetchAndLoadCatImage()

        if (isHoveringToolbarDown(locationOnScreen)) {
            showUtilView()
        } else if (chosenAgain) {
            hideUtilView()
        }
    }

    private fun fetchAndLoadCatImage() {
        mainRepository.fetchCatPic().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.loadCatView(it)
                }, {
                    Timber.e(it)
                    view.showError(it.message)
                }).addTo(subscriptions)
    }

    private fun showCatImage() {
        view.hideFilterView()
        view.showCatView()
    }

    override fun onRefreshButtonClick() {
        doAsync {
            mainRepository.refreshCars()
        }
    }

    private fun isHoveringToolbarDown(locationOnScreen: IntArray) =
            locationOnScreen[1] > (view.getScreenHeight() - view.getUtilDialogHeight())

    private fun hideUtilView() {
        view.animateHoveringToolbarDown()
        view.animateUtilViewHeightDown()
    }

    private fun showUtilView() {
        view.animateHoveringToolbarUp()
        view.animateUtilViewHeightUp()
    }
}