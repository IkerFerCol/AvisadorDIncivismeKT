package com.example.avisadordincivismekt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.avisadordincivisme.ui.home.HomeViewModel
import com.example.avisadordincivismekt.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var sharedViewModel: HomeViewModel
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ))
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)

        sharedViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedViewModel.setFusedLocationClient(mFusedLocationClient)

        sharedViewModel.getCheckPermission().observe(this) { checkPermission() }

        locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            when {
                fineLocationGranted -> sharedViewModel.startTrackingLocation(false)
                coarseLocationGranted -> sharedViewModel.startTrackingLocation(false)
                else -> Toast.makeText(this, "No concedeixen permisos", Toast.LENGTH_SHORT).show()
            }
        }
        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result: FirebaseAuthUIAuthenticationResult ->
            if (result.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                sharedViewModel.setUser(user!!)
            }
        }

    }

    private fun checkPermission() {
        Log.d("PERMISSIONS", "Check permissions")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSIONS", "Request permissions")
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            sharedViewModel.startTrackingLocation(false)
        }
    }

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        Log.e("XXXX", auth.currentUser.toString())
        if (auth.currentUser == null) {
            val signInIntent =
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(
                        Arrays.asList(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
                    .build()
            signInLauncher.launch(signInIntent)
        } else {
            sharedViewModel.setUser(auth.currentUser!!)
        }
    }



}