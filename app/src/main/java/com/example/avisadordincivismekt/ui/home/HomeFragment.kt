package com.example.avisadordincivisme.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avisadordincivismekt.databinding.FragmentHomeBinding
import com.example.avisadordincivismekt.ui.HomeViewModel
import com.google.firebase.auth.FirebaseUser


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
            binding.localitzacio.text = String.format(
                "Direcció: %1\$s \n Hora: %2\$tr",
                address, System.currentTimeMillis()
            )
        }

        sharedViewModel.getButtonText().observe(viewLifecycleOwner) { text ->
            binding.buttonLocation.text = text
        }

        sharedViewModel.getProgressBar().observe(viewLifecycleOwner) { visible ->
            binding.loading.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        binding.buttonLocation.setOnClickListener {
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


