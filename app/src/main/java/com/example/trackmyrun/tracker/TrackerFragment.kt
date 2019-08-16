package com.example.trackmyrun.tracker

import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.trackmyrun.databinding.TrackerFragmentBinding
import com.example.trackmyrun.R
import com.example.trackmyrun.database.MyRunDatabase
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
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

        val viewModelFactory = TrackerViewModelFactory(dataSource, application)

        // Get a reference to the ViewModel associated with this fragment.
        val trackerViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(TrackerViewModel::class.java)


        binding.trackerViewModel = trackerViewModel

        binding.lifecycleOwner = this

        val adapter = MyRunAdapter(MyRunListener { myRunId ->
            Toast.makeText(context,"${myRunId}",Toast.LENGTH_LONG).show()
        })

        binding.runList.adapter =adapter

        trackerViewModel.runs.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it)
            }
        })

        trackerViewModel.createAssets(application.applicationContext)

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
