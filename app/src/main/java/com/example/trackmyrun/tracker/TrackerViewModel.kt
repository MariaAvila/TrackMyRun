package com.example.trackmyrun.tracker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.trackmyrun.database.MyRun
import com.example.trackmyrun.database.MyRunDatabaseDao
import com.google.android.gms.location.*
import kotlinx.coroutines.*


class TrackerViewModel(
    dataSource: MyRunDatabaseDao,
    application: Application
) : ViewModel(){

    /*
    LOCATION TRACKING ATTRIBUTES #############################################################################
     */

    //Distance Travelled in total
    private var distanceTravelled = 0f

    //Ask for frequent location updates
    private var locationRequest : LocationRequest? = null

    //gets current location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //gets frequent location updates
    private lateinit var locationCallback: LocationCallback

    //holds last location accessed
    private var pastlocation : Location? = null

    //holds current location accessed
    private var currentlocation : Location? = null

    var isLocationOn = true

    /*
    DATABASE ATTRIBUTES #####################################################################################
     */

    //reference to database
    val database = dataSource

    //job that starts the coroutines
    private var viewModelJob = Job()

    //creates the coroutine scope
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //holds current run
    private var currentRun = MutableLiveData<MyRun?>()

    //holds all runs
    val runs = database.getAllRuns()

    //If the current run is not started, start button is visible
    val startButtonVisible = Transformations.map(currentRun){
        null == it
    }

    //If the current run is started, end button is visible
    val endButtonVisible = Transformations.map(currentRun){
        null != it
    }

    //If there are runs in the database, this button is visible
    val clearButtonVisible = Transformations.map(runs){
        it?.isNotEmpty()
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean?>()

    val showSnackBarEvent: LiveData<Boolean?>
        get() = _showSnackbarEvent

    private val _navigateToRunDetail= MutableLiveData<Long>()
    val navigateToRunDetail
        get() = _navigateToRunDetail

    /*
    INIT #####################################################################################################
     */

    init {
        currentRun.value = null
        initializeCurrentRun()
    }

    /*
    DATABASE FUNCTIONS ########################################################################################
     */

    //Initializes current run
    private fun initializeCurrentRun() {
        uiScope.launch {
            currentRun.value = getCurrentRunFromDatabase()
        }
    }

    //Function that gets the current run
    private suspend fun getCurrentRunFromDatabase(): MyRun? {
        return withContext(Dispatchers.IO) {
            var run = database.getLastRun()
            if (run?.endTimeMilli != run?.startTimeMilli) {
                run = null
            }
            run
        }
    }

    //inserts run into database
    private suspend fun insert(run : MyRun) {
        withContext(Dispatchers.IO) {
            database.insert(run)
        }
    }

    //updates run in database
    private suspend fun update(run : MyRun) {
        withContext(Dispatchers.IO) {
            database.update(run)
        }
    }

    //clears database
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    fun onStart() {
        distanceTravelled = 0f
        if (!isLocationOn) startLocationUpdates()
        uiScope.launch {
            // Create a new night, which captures the current time,
            // and insert it into the database.
            val newRun = MyRun()

            newRun.startPositionLat = pastlocation?.latitude!!
            newRun.startPositionLon = pastlocation?.longitude!!

            newRun.endPositionLat = pastlocation?.latitude!!
            newRun.endPositionLon = pastlocation?.longitude!!

            insert(newRun)

            currentRun.value = getCurrentRunFromDatabase()
        }
    }

    /**
     * Executes when the STOP button is clicked.
     */
    fun onStop() {
        stopLocationUpdates()
        isLocationOn = false
        uiScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch().
            val oldRun = currentRun.value ?: return@launch

            // Update the night in the database to add the end time.
            oldRun.endTimeMilli = System.currentTimeMillis()
            oldRun.distanceTravelled = distanceTravelled

            oldRun.endPositionLat = currentlocation?.latitude!!
            oldRun.endPositionLon = currentlocation?.longitude!!

            update(oldRun)
            initializeCurrentRun()

        }
    }

    /**
     * Executes when the CLEAR button is clicked.
     */
    fun onClear() {
        uiScope.launch {
            // Clear the database table.
            clear()

            // And clear tonight since it's no longer in the database
            currentRun.value = null

            // Show a snackbar message, because it's friendly.
            _showSnackbarEvent.value = true
        }
    }

    /**
     * Called when the ViewModel is dismantled.
     * At this point, we want to cancel all coroutines;
     * otherwise we end up with processes that have nowhere to return to
     * using memory and resources.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = null
    }

    fun onRunClicked(id: Long) {
        _navigateToRunDetail.value = id
    }

    fun onRunDetailNavigated() {
        _navigateToRunDetail.value = null
    }


    /*
    LOCATION TRACKING FUNCTIONS ############################################################################
     */

    //Function that requests the location updates to start
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            null /* Looper */)
    }

    //Function that requests the location updates to stop
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //Function that creates the location Request
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 5000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    //Function that creates the fusedLocationClient
    @SuppressLint("MissingPermission")
    fun createFusedLocationClient(context: Context?){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireNotNull(context))

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                pastlocation = location
                currentlocation = location
                //Log.i("tracker", pastlocation.toString())
            }
    }

    //Function that creates the locationCallback
    private fun createLocationCallback(){

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...

                    if (currentlocation != null) pastlocation = currentlocation
                    currentlocation = location

                    if (pastlocation != null && currentlocation != null)
                        distanceTravelled += pastlocation?.distanceTo(currentlocation)!!

                    //Log.i("tracker", distanceTravelled.toString())
                    //Log.i("tracker", currentlocation.toString())

                }
            }
        }
    }

    //function that creates all the assests needed to get location
    fun createAssets(context: Context?){
        createFusedLocationClient(context)
        createLocationCallback()
        createLocationRequest()
    }

}

