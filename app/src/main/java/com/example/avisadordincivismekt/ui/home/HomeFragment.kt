package com.example.avisadordincivismekt.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute.Location
import android.os.Bundle
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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var locationPermissionRequest: ActivityResultLauncher<Array<String>>? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLastLocation: Location? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
            Log.d("XXX", "Hola")

            if (fineLocationGranted || coarseLocationGranted) {

                getLocation()
            } else {
                Toast.makeText(requireContext(), "No se conceden permisos", Toast.LENGTH_SHORT).show()
            }
        }

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.localitzacio
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

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
            Toast.makeText(requireContext(), "getLocation: permissions granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getLastLocation() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}