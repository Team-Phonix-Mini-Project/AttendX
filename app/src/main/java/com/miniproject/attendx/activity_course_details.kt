package com.miniproject.attendx

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.databinding.ActivityCourseDetailsBinding

class activity_course_details : AppCompatActivity() {
    lateinit var binding:ActivityCourseDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCourseDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var courseID=intent.getStringExtra("courseid")
        var name=intent.getStringExtra("Name")
        var appl=intent.getStringExtra("User")
        appl="Total applicants : "+appl
        binding.courseDetailsCourseName.text=name
        binding.courseDetailsApplicants.text=appl
        binding.courseDetailsTakeAttendance.setOnClickListener {
            onTakeAttendanceButtonClicked(courseID,name)
        }
    }

    private fun onTakeAttendanceButtonClicked(courseID: String?, name: String?) {
        var intent=Intent(this,AttendanceTakingActivity::class.java)
        intent.putExtra("Name",name)
        intent.putExtra("courseid",courseID)
        startActivity(intent)
    }
}