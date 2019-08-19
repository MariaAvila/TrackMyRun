package com.example.trackmyrun.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.trackmyrun.databinding.TrackerFragmentBinding
import com.example.trackmyrun.R
import com.example.trackmyrun.database.MyRunDatabase
import com.google.android.material.snackbar.Snackbar



class TrackerFragment : Fragment(){


    companion object {
        fun newInstance() = TrackerFragment()
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {


        val binding: TrackerFragmentBinding = DataBindingUtil.inflate(inflater,
            R.layout.tracker_fragment,container,false)

        val application = requireNotNull(this.activity).application

        val dataSource = MyRunDatabase.getInstance(application).myRunDatabaseDao

        val viewModelFactory = TrackerViewModelFactory(dataSource)

        // Get a reference to the ViewModel associated with this fragment.
        val trackerViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(TrackerViewModel::class.java)

        binding.trackerViewModel = trackerViewModel

        binding.lifecycleOwner = this

        val adapter = MyRunAdapter(MyRunListener { myRunId ->
            //Toast.makeText(context,"${myRunId}",Toast.LENGTH_LONG).show()
            trackerViewModel.onRunClicked(myRunId)
        })

        binding.runList.adapter =adapter

        trackerViewModel.runs.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
            }
        })

        trackerViewModel.navigateToRunDetail.observe(this, Observer {run ->
            run?.let {
                this.findNavController().navigate(TrackerFragmentDirections.
                    actionTrackerFragmentToDetailsFragment(run))
                trackerViewModel.onRunDetailNavigated()
            }
        })

        trackerViewModel.createAssets(application.applicationContext)
        trackerViewModel.startLocationUpdates()
        trackerViewModel.isLocationOn = true

        trackerViewModel.showSnackBarEvent.observe(this, Observer {
            if (it == true) { // Observed state is true.
                Snackbar.make(
                    activity!!.findViewById(android.R.id.content),
                    "Data erased",
                    Snackbar.LENGTH_SHORT // How long to display the message.
                ).show()
                // Reset state to make sure the toast is only shown once, even if the device
                // has a configuration change.
                trackerViewModel.doneShowingSnackbar()
            }
        })

        //onClick listeners
        binding.startButton.setOnClickListener {
            trackerViewModel.onStart()
        }

        binding.endButton.setOnClickListener {
            trackerViewModel.onStop()
        }

        binding.clearButton.setOnClickListener {
            trackerViewModel.onClear()
        }

        return binding.root
    }


}
