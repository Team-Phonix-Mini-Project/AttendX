package com.miniproject.attendx.submitAttendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.attendance.markedDataObj
import com.miniproject.attendx.databinding.ActivitySubmitAttendanceItemBinding

class submit_attendance_recycleView_adapter(var arrayData:ArrayList<markedDataObj>) :
    RecyclerView.Adapter<submit_attendance_recycleView_adapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return arrayData.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ActivitySubmitAttendanceItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(arrayData[position])
    }

    inner class ViewHolder(val binding:ActivitySubmitAttendanceItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun onBind(markedDataObj: markedDataObj)
        {
            binding.submitAttenanceItemStudentName.text=markedDataObj.studentName
            binding.submitAttenanceItemStatus.text=markedDataObj.presentOrAbsent
        }

    }


}