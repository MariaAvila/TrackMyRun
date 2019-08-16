package com.example.trackmyrun.details

import androidx.lifecycle.*
import com.example.trackmyrun.database.MyRun
import com.example.trackmyrun.database.MyRunDatabaseDao
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class DetailsViewModel(private val myRunKey: Long = 0L, dataSource: MyRunDatabaseDao) : ViewModel() {

    val database = dataSource

    val run : LiveData<MyRun>

    init {
        run = database.getRunWithId(myRunKey)
    }

    private val _navigateToTracker = MutableLiveData<Boolean?>()
    val navigateToTracker: LiveData<Boolean?>
        get() = _navigateToTracker

    fun getMap(myMapView: MapView, lifecycleOwner: LifecycleOwner){
        val runToShow = database.getRunWithId(myRunKey)
        myMapView.getMapAsync { myMap ->
            val googleMap = myMap
            runToShow.observe(lifecycleOwner, Observer { run ->
                val startPoint = LatLng(run.startPositionLat, run.startPositionLon)
                googleMap.addMarker(
                    MarkerOptions().position(startPoint).title("Start Position").snippet("Your run started here")
                )
                val endPoint = LatLng(run.endPositionLat,run.endPositionLon)
                googleMap.addMarker(
                    MarkerOptions().position(endPoint).title("End Position").snippet("Your run ended here")
                )
                val cameraPosition = CameraPosition.Builder().target(startPoint).zoom(15f).build()
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            })
        }
    }

    fun doneNavigating() {
        _navigateToTracker.value = null
    }

    fun onClose() {
        _navigateToTracker.value = true
    }
}
