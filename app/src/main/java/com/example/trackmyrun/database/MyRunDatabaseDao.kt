package com.example.trackmyrun.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


/**
 * Defines methods for using the SleepNight class with Room.
 */
@Dao
interface MyRunDatabaseDao {

    @Insert
    fun insert(run: MyRun)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param run new value to write
     */
    @Update
    fun update(run: MyRun)

    /**
     * Selects and returns the row that matches the ID, which is our key.
     *
     * @param key ID to match
     */
    @Query("SELECT * from my_run_stats_table WHERE runId = :key")
    fun get(key: Long): MyRun

    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM my_run_stats_table")
    fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM my_run_stats_table ORDER BY runId DESC")
    fun getAllRuns(): LiveData<List<MyRun>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM my_run_stats_table ORDER BY runId DESC LIMIT 1")
    fun getLastRun(): MyRun?

    /**
     * Selects and returns the run with given runId.
     */
    @Query("SELECT * from my_run_stats_table WHERE runId = :key")
    fun getRunWithId(key: Long): LiveData<MyRun>
}