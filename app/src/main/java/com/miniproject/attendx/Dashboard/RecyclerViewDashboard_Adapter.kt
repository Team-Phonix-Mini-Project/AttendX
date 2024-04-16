package com.miniproject.attendx.Dashboard

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miniproject.attendx.course_details.activity_course_details
import com.miniproject.attendx.databinding.DashboardItemBinding


class RecyclerViewDashboard_Adapter(var data:ArrayList<objDashboard> ) :
    RecyclerView.Adapter<RecyclerViewDashboard_Adapter.ViewHolder>()
{

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DashboardItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(data[position],holder.binding.root.context)
    }
    inner class ViewHolder(val binding: DashboardItemBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun onBind(s: objDashboard, context: Context) {
            binding.textViewDashboardCardUserName.text=s.name;
            binding.textViewDashboardCardApplicantsNumber.text="Total Applicants : ${s.applicant}"
            binding.CardViewContainerId.setOnClickListener {
                var intent= Intent(context, activity_course_details::class.java)
                intent.putExtra("Name",s.name)
                intent.putExtra("User",s.applicant)
                intent.putExtra("courseid",s.courseId)
                context.startActivity(intent)
            }
        }
    }
}