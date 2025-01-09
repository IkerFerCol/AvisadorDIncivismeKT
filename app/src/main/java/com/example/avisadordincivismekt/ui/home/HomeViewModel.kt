package com.example.avisadordincivisme.ui.home
import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application

    private val currentAddress = MutableLiveData<String>()
    private val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()

    private var mTrackingLocation: Boolean = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                fetchAddress(location)
            }
        }
    }

    fun setFusedLocationClient(client: FusedLocationProviderClient) {
        mFusedLocationClient = client
    }

    fun getCurrentAddress(): LiveData<String> = currentAddress

    fun getButtonText(): LiveData<String> = buttonText

    fun getProgressBar(): LiveData<Boolean> = progressBar

    fun getCheckPermission(): LiveData<String> = checkPermission

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(needsChecking = true)
        } else {
            stopTrackingLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("listo")
        } else {
            mFusedLocationClient?.requestLocationUpdates(getLocationRequest(), mLocationCallback, null)
            currentAddress.postValue("Carregant...")
            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.value = "apaga el seguiment de la ubicacion"
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comença a seguir"
        }
    }

    private fun fetchAddress(location: Location) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var resultMessage = ""

            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses.isNullOrEmpty()) {
                    resultMessage = "No se encontro na"
                    Log.e("INCIVISME", resultMessage)
                } else {
                    val address = addresses[0]
                    val addressParts = (0..address.maxAddressLineIndex).map { address.getAddressLine(it) }
                    resultMessage = addressParts.joinToString("\n")
                    val finalResultMessage = resultMessage
                    handler.post {
                        if (mTrackingLocation) {
                            currentAddress.postValue(
                                "Direcció: $finalResultMessage \n Hora: ${System.currentTimeMillis()}"
                            )
                        }
                    }
                }
            } catch (ioException: IOException) {
                resultMessage = "Servei no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Coordenades no valides"
                Log.e(
                    "INCIVISME",
                    "$resultMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}",
                    illegalArgumentException
                )
            }
        }
    }

    fun get(java: Class<HomeViewModel>) {

    }
}