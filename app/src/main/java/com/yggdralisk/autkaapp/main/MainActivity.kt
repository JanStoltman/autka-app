package com.yggdralisk.autkaapp.main

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.jakewharton.rxbinding2.view.RxView
import com.yggdralisk.autkaapp.R
import com.yggdralisk.autkaapp.common.anim.HeightProperty
import com.yggdralisk.autkaapp.data.network.model.CarModel
import com.yggdralisk.autkaapp.data.network.model.Owner
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), MainContract.View, OnMapReadyCallback {
    companion object {
        private const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1
        private const val ANIM_DURATION = 500L
    }

    @Inject
    lateinit var presenter: MainContract.Presenter
    var map: GoogleMap? = null
    private val subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachOnClicks()
        presenter.onStart()
        fetchMap()
        if (checkLocationPermission().not()) {
            presenter.onLocationPermissionNotGranted()
        }
    }

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

                                zoomToLocation(location, it)
                            }
                        }
                    }
        }
    }

    private fun zoomToLocation(location: Location, map: GoogleMap) {
        val cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(LatLng(location.latitude, location.longitude), 15f)
        map.animateCamera(cameraUpdate)
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

    override fun showError(message: String?) {
        contentView?.let {
            toast(message ?: getString(R.string.generic_error_message))
        }
    }

    override fun displayCars(cars: List<CarModel>?) {
        if (map != null) {
            map?.clear()
            cars?.let {
                //Prepare bitmaps to avoid fetching them in a loop
                val vozillaCarIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_vozilla_car)
                val traficarCarIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_traficar_car)

                for (car in it) {
                    map?.addMarker(MarkerOptions()
                            .position(LatLng(car.Latitude, car.Longitude))
                            .icon(if (car.Owner == Owner.TRAFICAR.ownerName) {
                                traficarCarIcon
                            } else {
                                vozillaCarIcon
                            }))
                }
            }
        }
    }

    fun onFilterButtonClick(v: View) {
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        presenter.onFilterButtonClick(location)
        //TODO: add anim reverse icon
    }

    fun onRefreshButtonClick(v: View) {
        presenter.onRefreshButtonClick()
        //TODO: add anim spinning
    }

    fun onLocationButtonClick(v: View) {
        //TODO: add squeeze anim
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
}

//todo hide hover toolbar on back

