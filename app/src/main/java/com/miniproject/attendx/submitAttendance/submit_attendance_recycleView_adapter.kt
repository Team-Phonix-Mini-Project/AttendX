package com.miniproject.attendx.submitAttendance

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.R
import com.miniproject.attendx.attendance.markedDataObj
import com.miniproject.attendx.databinding.ActivitySubmitAttendanceItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class submit_attendance_recycleView_adapter(var arrayData:ArrayList<markedDataObj>) :
    RecyclerView.Adapter<submit_attendance_recycleView_adapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return arrayData.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ActivitySubmitAttendanceItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(arrayData[position],holder.binding.root.context)
    }

    inner class ViewHolder(val binding:ActivitySubmitAttendanceItemBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun onBind(markedDataObj: markedDataObj,context:Context)
        {
            binding.submitAttenanceItemStudentName.text=markedDataObj.studentName
            binding.submitAttenanceItemStatus.text=markedDataObj.presentOrAbsent
            updateStatusColor(binding)
            binding.submitAttenanceItemChangeStatusButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle("Change attendance status for ${markedDataObj.studentName}")
                    .setPositiveButton("YES"){_,_->
                        if(markedDataObj.presentOrAbsent=="PRESENT")
                        {
                            markAttendance((markedDataObj.statusID.toInt()+1).toString(),markedDataObj.studentID,1,markedDataObj.studentID,markedDataObj.sessionID)
                            binding.submitAttenanceItemStatus.text="ABSENT"
                            binding.submitAttenanceItemStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.absent_color))
                        }
                        else
                        {
                            markAttendance((markedDataObj.statusID.toInt()-1).toString(),markedDataObj.studentID,1,markedDataObj.studentID,markedDataObj.sessionID)
                            binding.submitAttenanceItemStatus.text="PRESENT"
                            binding.submitAttenanceItemStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.present_color))
                        }
                    }
                    .setNegativeButton("CANCEL"){_,_->

                    }
                    .show()

            }
        }

    }

    private fun markAttendance(
        statusIdPresent: String,
        studentID: String,
        statusset: Int,
        takenByid: String,
        sessionID: String
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
            val params = mapOf(
                "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
                "wsfunction" to "mod_attendance_update_user_status",
                "moodlewsrestformat" to "json",
                "sessionid" to sessionID,
                "studentid" to studentID,
                "takenbyid" to takenByid,
                "statusid" to statusIdPresent,
                "statusset" to statusset.toString()
            )
            val formBody = FormBody.Builder()
            params.forEach { (key, value) ->
                formBody.add(key, value)
            }
            val request = Request.Builder()
                .url(url)
                .post(formBody.build())
                .build()

            try {
                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("markAttendance", "Failed to mark attendance for student $studentID")
                }
            } catch (e: IOException) {
                Log.e("markAttendance", "Network error: ${e.message}")
            }
        }
    }
    fun updateStatusColor(binding: ActivitySubmitAttendanceItemBinding)
    {
        if(binding.submitAttenanceItemStatus.text=="PRESENT")
        {
            binding.submitAttenanceItemStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green))
        }
        else
        {
            binding.submitAttenanceItemStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
        }
    }


}