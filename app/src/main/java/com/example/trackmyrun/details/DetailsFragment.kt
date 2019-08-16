package com.example.trackmyrun.details

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.trackmyrun.R
import com.example.trackmyrun.database.MyRunDatabase
import com.example.trackmyrun.databinding.DetailsFragmentBinding
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import java.lang.Exception


class DetailsFragment : Fragment() {

    lateinit var myMapView : MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DetailsFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.details_fragment, container, false
        )

        myMapView = binding.mapView
        myMapView.onCreate(savedInstanceState)
        myMapView.onResume()

        val application = requireNotNull(this.activity).application
        val arguments = DetailsFragmentArgs.fromBundle(arguments!!)

        val dataSource = MyRunDatabase.getInstance(application).myRunDatabaseDao
        val viewModelFactory = DetailsViewModelFactory(arguments.runId, dataSource)

        val detailsViewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(DetailsViewModel::class.java)

        binding.detailsViewModel = detailsViewModel

        binding.lifecycleOwner = this


        //Map building

        try {
            MapsInitializer.initialize(application.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        detailsViewModel.getMap(myMapView,this)

        //observers
        detailsViewModel.navigateToTracker.observe(this, Observer {
            if (it == true){
                this.findNavController().navigate(
                    DetailsFragmentDirections.actionDetailsFragmentToTrackerFragment())
                detailsViewModel.doneNavigating()
            }
        })

        detailsViewModel.run.observe(this, Observer { run ->
            if(run.startTimeMilli == run.endTimeMilli){
                binding.durationTextView.text = convertDurationToFormatted(run.startTimeMilli, System.currentTimeMillis(),application.resources)
                binding.distanceTextView.text = getString(R.string.still_running)
            }
            else{
                binding.durationTextView.text = convertDurationToFormatted(run.startTimeMilli,run.endTimeMilli,application.resources)
                binding.distanceTextView.text = getString(R.string.distance_length,run.distanceTravelled.toInt())
            }
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        myMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        myMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        myMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        myMapView.onLowMemory()
    }


}
