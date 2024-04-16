package com.miniproject.attendx.submitAttendance

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.Dashboard.RecyclerViewDashboard_Adapter
import com.miniproject.attendx.attendance.markedDataObj
import com.miniproject.attendx.databinding.ActivitySubmitAttendanceItemBinding

class submit_attendance_recycleView_adapter(var arrayData:ArrayList<markedDataObj>) :
    RecyclerView.Adapter<RecyclerViewDashboard_Adapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return arrayData.size
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewDashboard_Adapter.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerViewDashboard_Adapter.ViewHolder, position: Int) {

    }
    inner class ViewHolder(val binding:ActivitySubmitAttendanceItemBinding) :
        RecyclerView.ViewHolder(binding.root){
            fun onBind()
            {

            }

        }

}