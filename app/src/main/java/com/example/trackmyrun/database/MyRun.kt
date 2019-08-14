package com.example.trackmyrun.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "my_run_stats_table")
data class MyRun(
    @PrimaryKey(autoGenerate = true)
    var runId: Long = 0L,

    @ColumnInfo(name = "start_time_milli")
    val startTimeMilli: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time_milli")
    var endTimeMilli: Long = startTimeMilli,

    @ColumnInfo(name = "start_position_lat")
    var startPositionLat: Double = 0.0,

    @ColumnInfo(name = "start_position_lon")
    var startPositionLon: Double = 0.0,

    @ColumnInfo(name = "end_position_lat")
    var endPositionLat: Double = 0.0,

    @ColumnInfo(name = "end_position_lon")
    var endPositionLon: Double = 0.0,

    @ColumnInfo(name = "distance_travelled")
    var distanceTravelled: Float = -1f
)