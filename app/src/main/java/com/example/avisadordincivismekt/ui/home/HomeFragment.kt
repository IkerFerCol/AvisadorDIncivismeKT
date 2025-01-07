package com.example.avisadordincivismekt.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avisadordincivismekt.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            Log.d("XXX", "Permisos concedidos")

            if (fineLocationGranted || coarseLocationGranted) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "No se conceden permisos", Toast.LENGTH_SHORT).show()
            }
        }

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonLocation.setOnClickListener {
            getLocation()
        }

        return root
    }

    private fun getLocation() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Request Permissions", Toast.LENGTH_SHORT).show()
            locationPermissionRequest?.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            mFusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    mLastLocation = location

                    val latitude = location.latitude
                    val longitude = location.longitude
                    val time = location.time


                    val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                    val formattedTime = dateFormat.format(Date(time))


                    binding.localitzacio.text = String.format(
                        "Latitud: %.4f \n Longitud: %.4f\n Hora: %s",
                        latitude,
                        longitude,
                        formattedTime
                    )


                    fetchAddress(latitude, longitude)

                } else {
                    binding.localitzacio.text = "Sin localización conocida"
                }
            }
        }
    }

    private fun fetchAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addresses: List<Address>? = null
        var resultMessage = ""

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                val addressParts = ArrayList<String>()

                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }

                resultMessage = addressParts.joinToString("\n")
            } else {
                resultMessage = "Direcció no disponible"
            }
        } catch (e: Exception) {
            Log.e("fetchAddress", "Error al obtenir la direcció", e)
            resultMessage = "Error al obtenir la direcció"
        }

        binding.localitzacio.text = resultMessage
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
