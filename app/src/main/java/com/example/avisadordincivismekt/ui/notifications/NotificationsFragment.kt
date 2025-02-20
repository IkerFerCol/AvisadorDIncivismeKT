package com.example.avisadordincivismekt.ui.notifications

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.avisadordincivismekt.R
import com.example.avisadordincivismekt.databinding.FragmentNotificationsBinding
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var mCurrentPhotoPath: String = ""
    private var photoURI: Uri? = null
    private var foto: ImageView? = null
    private val REQUEST_TAKE_PHOTO = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.MapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.MapView.setMultiTouchControls(true)
        val mapController = binding.MapView.controller
        mapController.setZoom(14.5)
        val startPoint = GeoPoint(37.4219983, -122.084)
        mapController.setCenter(startPoint)

        val myLocationNewOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.MapView)
        myLocationNewOverlay.enableMyLocation()
        binding.MapView.overlays.add(myLocationNewOverlay)

        val compassOverlay = CompassOverlay(requireContext(), InternalCompassOrientationProvider(requireContext()), binding.MapView)
        compassOverlay.enableCompass()
        binding.MapView.overlays.add(compassOverlay)

        requestPermissionsIfNecessary(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))

        val foto: ImageView = requireView().findViewById(R.id.cross)
        val buttonFoto: Button = requireView().findViewById(R.id.fotobutton)

        buttonFoto.setOnClickListener {
            dispatchTakePictureIntent()
        }


        return root
    }

    override fun onResume() {
        super.onResume()
        binding.MapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.MapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest = ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp"
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) { // (1)
            var photoFile: File? = null
            try {
                photoFile = createImageFile() // (2)
            } catch (ex: IOException) {
                // Manejo de la excepción (3)
                ex.printStackTrace() // Puedes manejar el error de otra manera si lo deseas
            }

            if (photoFile != null) { // (4)
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.android.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI) // (5)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO) // (6)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoURI).into(foto!!)
            } else {
                Toast.makeText(requireContext(),
                    "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}