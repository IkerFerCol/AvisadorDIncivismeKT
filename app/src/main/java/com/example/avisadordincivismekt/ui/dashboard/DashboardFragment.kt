package com.example.avisadordincivismekt.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avisadordincivismekt.Incidencia
import com.example.avisadordincivismekt.databinding.FragmentDashboardBinding
import com.example.avisadordincivismekt.databinding.RvIncidenciesItemBinding
import com.example.avisadordincivismekt.ui.HomeViewModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val sharedViewModel =
            ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user

            if (user != null) {
                val base = FirebaseDatabase.getInstance().getReference()

                val users = base.child("users")
                val uid = users.child(authUser!!.uid)
                val incidencies = uid.child("incidencies")

                val options = FirebaseRecyclerOptions.Builder<Incidencia>()
                    .setQuery(incidencies, Incidencia::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()

                val adapter = IncidenciaAdapter(options)
                binding.rvIncidencies.adapter = adapter
                binding.rvIncidencies.layoutManager = LinearLayoutManager(requireContext())
                adapter.startListening()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class IncidenciaAdapter(options: FirebaseRecyclerOptions<Incidencia>) :
        FirebaseRecyclerAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewHolder>(options) {

        override fun onBindViewHolder(
            holder: IncidenciaViewHolder, position: Int, model: Incidencia
        ) {
            holder.binding.txtDescripcio.text = model.problema
            holder.binding.txtAdreca.text = model.direccio
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
            val binding = RvIncidenciesItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return IncidenciaViewHolder(binding)
        }

        class IncidenciaViewHolder(val binding: RvIncidenciesItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
