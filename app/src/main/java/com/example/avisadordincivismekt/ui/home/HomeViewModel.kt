package com.example.avisadordincivismekt.ui.home


import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application

    companion object {
        private val currentAddress = MutableLiveData<String>()
    }

    private val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()

    private var mTrackingLocation = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    fun setFusedLocationClient(client: FusedLocationProviderClient) {
        mFusedLocationClient = client
    }

    fun getCurrentAddress(): LiveData<String> = currentAddress

    fun getButtonText(): MutableLiveData<String> = buttonText

    fun getProgressBar(): MutableLiveData<Boolean> = progressBar

    fun getCheckPermission(): LiveData<String> = checkPermission

    private val mLocationCallback = object : LocationCallback() {
        fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.lastLocation?.let { fetchAddress(it) }
        }
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true)
        } else {
            stopTrackingLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("check")
        } else {
            mFusedLocationClient?.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback, null
            )

            currentAddress.postValue("Carregant...")
            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.value = "Aturar el seguiment de la ubicació"
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comença a seguir la ubicació"
        }
    }

    private fun fetchAddress(location: Location) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var addresses: List<Address>? = null
            var resultMessage = ""

            try {
                addresses = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1
                )

                if (addresses.isNullOrEmpty()) {
                    resultMessage = "No s'ha trobat cap adreça"
                    Log.e("INCIVISME", resultMessage)
                } else {
                    val address = addresses[0]
                    val addressParts = ArrayList<String>()

                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }

                    resultMessage = TextUtils.join("\n", addressParts)
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
                resultMessage = "Coordenades no vàlides"
                Log.e(
                    "INCIVISME", "$resultMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}",
                    illegalArgumentException
                )
            }
        }
    }
}
