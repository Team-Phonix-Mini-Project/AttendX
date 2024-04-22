package com.miniproject.attendx.course_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.PresentyShowItemBinding

class presenty_showing_RecyclerView_adapter(var data: ArrayList<data_attendance_report_show_object>) :
    RecyclerView.Adapter<presenty_showing_RecyclerView_adapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PresentyShowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    inner class ViewHolder(var binding: PresentyShowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(s: data_attendance_report_show_object) {
            if (s.studentName == "not") {
                binding.presentyShowingStudentName.text = "No attendance was marked in this session"
                binding.presentyShowingPresentyStatus.visibility = View.GONE
            } else {
                binding.presentyShowingStudentName.text = s.studentName
                binding.presentyShowingPresentyStatus.text = s.status
                updateStatusColor(binding)
            }
        }
    }

    fun updateStatusColor(binding: PresentyShowItemBinding) {
        if (binding.presentyShowingPresentyStatus.text == "PRESENT") {
            binding.presentyShowingPresentyStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.green
                )
            )
        } else {
            binding.presentyShowingPresentyStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.red
                )
            )
        }
    }


}