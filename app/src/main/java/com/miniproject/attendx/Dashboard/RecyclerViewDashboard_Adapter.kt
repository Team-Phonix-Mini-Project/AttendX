package com.miniproject.attendx.Dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.databinding.DashboardItemBinding


class RecyclerViewDashboard_Adapter(val items:Array<String>) :
    RecyclerView.Adapter<RecyclerViewDashboard_Adapter.ViewHolder>()
{

    override fun getItemCount(): Int {

        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DashboardItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.onBind(items[position])
    }

    inner class ViewHolder(val binding: DashboardItemBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun onBind(s: String)
        {
            binding.textViewDashboardCard.text=s.toString()
        }
    }
}