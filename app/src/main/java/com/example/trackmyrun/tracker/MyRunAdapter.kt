package com.example.trackmyrun.tracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.convertDurationToFormatted
import com.example.trackmyrun.R
import com.example.trackmyrun.database.MyRun

class MyRunAdapter: RecyclerView.Adapter<MyRunAdapter.ViewHolder>()  {

    var data =  listOf<MyRun>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val res = holder.itemView.context.resources
        holder.runLength.text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, res)
        holder.distance.text = "Meters runned:" + item.distanceTravelled.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.list_item_run, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val runLength: TextView = itemView.findViewById(R.id.run_length)
        val distance: TextView = itemView.findViewById(R.id.distance_runned)
    }

}