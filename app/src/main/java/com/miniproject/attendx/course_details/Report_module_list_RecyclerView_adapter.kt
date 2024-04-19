package com.miniproject.attendx.course_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.attendance.attendance_module_list_object
import com.miniproject.attendx.databinding.AttReportModuleItemBinding

interface AttendanceModuleClickListener_report {
    fun onAttendanceModuleClicked(attendanceModule: attendance_module_list_object)
}

class Report_module_list_RecyclerView_adapter(var data:ArrayList<attendance_module_list_object>,private val listener: AttendanceModuleClickListener_report):RecyclerView.Adapter<Report_module_list_RecyclerView_adapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AttReportModuleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.Bind(data[position])
    }

    inner class ViewHolder(var binding: AttReportModuleItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            // Move the code for setting text outside of the click listener
            binding.attReportModuleName.text = "" // Clear previous text, if any
            binding.attReportModuleItemContainer.setOnClickListener {
                // Handle click event if needed
                listener.onAttendanceModuleClicked(data[adapterPosition])
            }
        }

        fun Bind(attendanceModuleListObject: attendance_module_list_object) {
            // Move the code for setting text outside of the click listener
            binding.attReportModuleName.text = attendanceModuleListObject.attName
        }
    }
}