package com.example.trackmyrun.tracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trackmyrun.database.MyRun
import com.example.trackmyrun.databinding.ListItemRunBinding

class MyRunAdapter(val clickListener: MyRunListener):
    ListAdapter<MyRun, MyRunAdapter.ViewHolder>(MyRunDiffCallback()) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder(val binding: ListItemRunBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: MyRun, clickListener: MyRunListener) {
            binding.run = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemRunBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}
class MyRunDiffCallback : DiffUtil.ItemCallback<MyRun>(){
    override fun areItemsTheSame(oldItem: MyRun, newItem: MyRun): Boolean {
        return oldItem.runId == newItem.runId
    }

    override fun areContentsTheSame(oldItem: MyRun, newItem: MyRun): Boolean {
        return oldItem == newItem
    }

}
class MyRunListener(val clickListener: (myRunId: Long) -> Unit){
    fun onClick(myRun: MyRun) = clickListener(myRun.runId)
}