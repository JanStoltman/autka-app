package com.yggdralisk.autkaapp.main

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import com.yggdralisk.autkaapp.R
import com.yggdralisk.autkaapp.common.anim.HeightProperty
import com.yggdralisk.autkaapp.common.extension.*
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.Owner
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.bottom_sheet_main.*
import kotlinx.android.synthetic.main.bottom_sheet_traficar.*
import kotlinx.android.synthetic.main.bottom_sheet_unknown.*
import kotlinx.android.synthetic.main.bottom_sheet_vozilla.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), MainContract.View, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    companion object {
        private const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1
        private const val ANIM_DURATION = 500L
    }

    @Inject
    lateinit var presenter: MainContract.Presenter
    var map: GoogleMap? = null
    private val subscriptions = CompositeDisposable()
    private lateinit var carDetailsLayoutBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        carDetailsLayoutBehavior = BottomSheetBehavior.from(carDetailsLayout)
        attachOnClicks()
        presenter.onStart()
        fetchMap()
        if (checkLocationPermission().not()) {
            presenter.onLocationPermissionNotGranted()
        }
    }

    override fun onStart() {
        super.onStart()
        hideBottomSheet()
    }

    override fun onBackPressed() {
        if (presenter.onBackPressed().not()) {
            super.onBackPressed()
        }
    }

    override fun hideBottomSheet() =
            carDetailsLayoutBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)

    override fun carDetailsLayoutBehaviorIsHidden(): Boolean =
            carDetailsLayoutBehavior.state == BottomSheetBehavior.STATE_HIDDEN


    private fun attachOnClicks() {
        vozillaFilterSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onVozillaSelectionChanged(isChecked) }
        traficarFilterSwitch.setOnCheckedChangeListener { _, isChecked -> presenter.onTraficarSelectionChanged(isChecked) }
        RxView.clicks(filterButton).throttleFirst(ANIM_DURATION, TimeUnit.MILLISECONDS).subscribe { onFilterButtonClick(filterButton) }.addTo(subscriptions)
        RxView.clicks(refreshButton).throttleFirst(15, TimeUnit.SECONDS).subscribe { onRefreshButtonClick(refreshButton) }.addTo(subscriptions)
        RxView.clicks(locationButton).throttleFirst(ANIM_DURATION, TimeUnit.MILLISECONDS).subscribe { onLocationButtonClick(locationButton) }.addTo(subscriptions)
    }

    override fun onDestroy() {
        subscriptions.clear()
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun fetchMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
        if (mapFragment != null && mapFragment is SupportMapFragment) {
            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        presenter.onMapReady()
        this.map = map

        map?.let {
            it.uiSettings.isMyLocationButtonEnabled = false
            it.uiSettings.isMapToolbarEnabled = false
            it.uiSettings.isCompassEnabled = false
            it.uiSettings.isIndoorLevelPickerEnabled = false
            it.setOnMarkerClickListener(this)
            it.setOnMapClickListener { _ -> presenter.onMapClick() }
            it.moveToWroclaw()
        }
    }

    override fun fetchAndDisplayLatestLocation() {
        if (checkLocationPermission()) {
            LocationServices.getFusedLocationProviderClient(this)
                    .lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            map?.let {
                                it.isMyLocationEnabled = true

                                it.zoomToLocation(location)
                            }
                        }
                    }
        }
    }

    override fun checkLocationPermission(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    presenter.onLocationPermissionGranted()
                } else {
                    presenter.onLocationPermissionDenied()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun showError() {
        contentView?.let {
            toast(getString(R.string.generic_error_message))
        }
    }

    override fun displayCars(cars: List<CarModel>?) {
        if (map != null) {
            map?.clear()
            cars?.let {
                val vozillaCarIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_vozilla_car)
                val traficarCarIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_traficar_car)
                val unknownCarIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_unknown_car)

                for (car in it) {
                    map?.addMarker(MarkerOptions()
                            .position(car.toLatLng())
                            .icon(when {
                                car.isOwnedBy(Owner.TRAFICAR) -> traficarCarIcon
                                car.isOwnedBy(Owner.VOZILLA) -> vozillaCarIcon
                                else -> unknownCarIcon
                            }))?.tag = car
                }
            }
        }
    }

    fun onFilterButtonClick(v: View) {
        presenter.onFilterButtonClick(getViewsLocationOnScreen(v))
    }

    private fun getViewsLocationOnScreen(v: View): IntArray {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        return location
    }

    fun onRefreshButtonClick(v: View) {
        presenter.onRefreshButtonClick()
    }

    fun onLocationButtonClick(v: View) {
        presenter.onLocationButtonClick()
    }

    override fun animateHoveringToolbarUp() {
        val dialogHeight = getUtilDialogHeight().times(-1)
        val animation = ObjectAnimator.ofFloat(hoveringToolbar, "translationY", dialogHeight)
        animation.duration = ANIM_DURATION
        animation.start()
    }

    override fun animateHoveringToolbarDown() {
        val dialogHeight = 0f
        val animation = ObjectAnimator.ofFloat(hoveringToolbar, "translationY", dialogHeight)
        animation.duration = ANIM_DURATION
        animation.start()
    }

    override fun animateUtilViewHeightUp() {
        val height = PropertyValuesHolder.ofFloat(HeightProperty(), 0f, getUtilDialogViewHeight())
        val visibility = PropertyValuesHolder.ofFloat("alpha", 1f)
        val anim = ObjectAnimator.ofPropertyValuesHolder(utilView, height, visibility)
        anim.duration = ANIM_DURATION
        anim.start()
    }

    override fun animateUtilViewHeightDown() {
        val height = PropertyValuesHolder.ofFloat(HeightProperty(), 0f)
        val visibility = PropertyValuesHolder.ofFloat("alpha", 0f)
        val anim = ObjectAnimator.ofPropertyValuesHolder(utilView, height, visibility)
        anim.duration = ANIM_DURATION
        anim.start()
    }

    override fun getUtilDialogHeight(): Float = resources.getDimension(R.dimen.utilDialogHeight)

    fun getUtilDialogViewHeight(): Float = resources.getDimension(R.dimen.utilViewHeight)

    override fun getScreenHeight(): Int {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    override fun isVozillaSwitchChecked(): Boolean = vozillaFilterSwitch.isChecked

    override fun isTraficarSwitchChecked(): Boolean = traficarFilterSwitch.isChecked

    override fun onMarkerClick(marker: Marker?): Boolean = presenter.onMarkerClick(marker)

    override fun showDetailsView(car: CarModel) {
        map?.zoomToLatLngFromTop(car.toLatLng())
        carDetailsLayoutBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        when {
            car.isOwnedBy(Owner.TRAFICAR) -> {
                showTraficarDetailsView(car)
            }
            car.isOwnedBy(Owner.VOZILLA) -> {
                showVozillaDetailsView(car)
            }
            else -> {
                showUnknownDetailsView(car)
            }
        }
    }

    private fun showUnknownDetailsView(car: CarModel) {
        changeBottomSheetsVisibility(unknownBottomSheet)
        fillCommonInfo(unknownBottomSheet, car)
    }

    private fun showVozillaDetailsView(car: CarModel) {
        changeBottomSheetsVisibility(vozillaBottomSheet)
        fillCommonInfo(vozillaBottomSheet, car)

        vozillaBottomSheet.findViewById<TextView>(R.id.rangeTV)?.text = getString(R.string.km, car.rangeKm.toString())
        vozillaBottomSheet.findViewById<View>(R.id.providerButton)?.setOnClickListener { openProviderApp(Owner.VOZILLA) }
    }

    private fun showTraficarDetailsView(car: CarModel) {
        changeBottomSheetsVisibility(traficarBottomSheet)
        fillCommonInfo(traficarBottomSheet, car)

        traficarBottomSheet.findViewById<TextView>(R.id.fuelTV)?.text = getString(R.string.l, car.fuel.toString())
        traficarBottomSheet.findViewById<View>(R.id.providerButton)?.setOnClickListener { openProviderApp(Owner.TRAFICAR) }
    }

    private fun changeBottomSheetsVisibility(visibleBottomSheet: ViewGroup) {
        unknownBottomSheet.makeGone()
        traficarBottomSheet.makeGone()
        vozillaBottomSheet.makeGone()
        visibleBottomSheet.makeVisible()
    }

    private fun fillCommonInfo(visibleBottomSheet: ViewGroup, car: CarModel) {
        visibleBottomSheet.findViewById<TextView>(R.id.modelTV)?.text = car.model
        visibleBottomSheet.findViewById<TextView>(R.id.plateNumberTV)?.text = car.plateNumber
        visibleBottomSheet.findViewById<TextView>(R.id.sideNumberTV)?.text = car.sideNumber
    }

    override fun openProviderApp(owner: Owner) {
        val launchIntent = when (owner) {
            Owner.VOZILLA -> {
                packageManager.getLaunchIntentForPackage("pl.techgarden.vozilla")
            }
            Owner.TRAFICAR -> {
                packageManager.getLaunchIntentForPackage("pl.express.traficar")
            }
        }

        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }

    override fun isHoveringToolbarDown() =
            getViewsLocationOnScreen(filterButton)[1] > (getScreenHeight() - getUtilDialogHeight())
}