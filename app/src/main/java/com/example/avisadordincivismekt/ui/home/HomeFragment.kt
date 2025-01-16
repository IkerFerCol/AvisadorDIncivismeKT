package com.example.avisadordincivisme.ui.home

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avisadordincivismekt.Incidencia
import com.example.avisadordincivismekt.databinding.FragmentHomeBinding
import com.example.avisadordincivismekt.ui.HomeViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        sharedViewModel.getCurrentAddress().observe(viewLifecycleOwner) { address ->
            binding.txtDireccio.text = Editable.Factory.getInstance().newEditable(
                String.format(
                    "Direcció: %1\$s \n Hora: %2\$tr",
                    address.toString(), System.currentTimeMillis()
                )
            )
        }

        sharedViewModel.getcurrentLatLng().observe(viewLifecycleOwner) { latlng ->
            binding.txtLatitud.setText(java.lang.String.valueOf(latlng.latitude))
            binding.txtLongitud.setText(java.lang.String.valueOf(latlng.longitude))
        }



        sharedViewModel.getProgressBar().observe(
            viewLifecycleOwner
        ) { visible: Boolean ->
            if (visible) binding.loading.visibility = ProgressBar.VISIBLE
            else binding.loading.visibility = ProgressBar.INVISIBLE
        }
        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        binding.buttonNotificar.setOnClickListener {
            Log.d("DEBUG", "Clicked Get Location")
            sharedViewModel.switchTrackingLocation()
        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


