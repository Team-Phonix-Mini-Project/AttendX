package com.miniproject.attendx.course_details

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.databinding.ReportSessionListItemBinding

class report_session_list_adapter(var data: ArrayList<sessionIdAndNameData_Object>) :
    RecyclerView.Adapter<report_session_list_adapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReportSessionListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(data[position], holder.binding.root.context)
    }

    inner class ViewHolder(var binding: ReportSessionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(s: sessionIdAndNameData_Object, context: Context) {
            if (s.date == "not") {
                binding.reportSessionListItemDateText.text =
                    "No sessions are created in current course"
                binding.reportSessionListItemButtonCheck.visibility = View.GONE
            } else {
                binding.reportSessionListItemDateText.text = s.date
                binding.reportSessionListItemButtonCheck.setOnClickListener {
                    var intent = Intent(context, session_presenty_displaying_activity::class.java)
                    intent.putExtra("sessionid", s.sessionId)
                    intent.putExtra("date", s.date)
                    intent.putExtra("courseid", s.courseID)
                    intent.putExtra("coursename", s.courseName)
                    context.startActivity(intent)
                }
            }
        }
    }


}