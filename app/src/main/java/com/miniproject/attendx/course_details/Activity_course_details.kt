package com.miniproject.attendx.course_details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miniproject.attendx.R
import com.miniproject.attendx.attendance.AttendanceTakingActivity
import com.miniproject.attendx.attendance.attendance_module_list_object
import com.miniproject.attendx.databinding.ActivityCourseDetailsBinding
import com.miniproject.attendx.databinding.AttReportModuleNameListBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class activity_course_details : AppCompatActivity() {
    lateinit var binding: ActivityCourseDetailsBinding
    var dataModuleName = arrayListOf<attendance_module_list_object>()
    lateinit var AttModID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var courseID = intent.getStringExtra("courseid")
        var name = intent.getStringExtra("Name")
        var appl = intent.getStringExtra("User")
        appl = "Total applicants : " + appl
        binding.courseDetailsCourseName.text = name
        binding.courseDetailsApplicants.text = appl
        binding.courseDetailsTakeAttendance.setOnClickListener {
            onTakeAttendanceButtonClicked(courseID, name)
        }
        binding.courseDetailsGetAttendanceReport.setOnClickListener {
            onGetAttendanceReportClicked(courseID, name)
        }
    }

    private fun onTakeAttendanceButtonClicked(courseID: String?, name: String?) {
        var intent = Intent(this, AttendanceTakingActivity::class.java)
        intent.putExtra("Name", name)
        intent.putExtra("courseid", courseID)
        startActivity(intent)
    }

    private fun onGetAttendanceReportClicked(courseID: String?, courseName: String?) {
        fetchAttendanceModuleID(courseID.toString()){attendanceMod->
            AttModID=attendanceMod
             var intent=Intent(this,ReportOfAttendanceModuleSessionList::class.java)
            intent.putExtra("attname","Attendance")
            intent.putExtra("attid",AttModID)
            intent.putExtra("courseid",courseID)
            intent.putExtra("coursename",courseName)
            startActivity(intent)
        }
    }

    private fun fetchAttendanceModuleID(courseID: String,callback: (String) -> Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "core_course_get_contents",
            "moodlewsrestformat" to "json",
            "courseid" to courseID
        )
        val formBody = FormBody.Builder()
        for ((key, value) in params) {
            formBody.add(key, value)
        }
        val request = Request.Builder().url(url).post(formBody.build()).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val datas = JSONArray(responseBody)
                    val data=datas.getJSONObject(0)
                    var array=data.getJSONArray("modules")
                    for (i in 0 until array.length()) {
                        var obj=array.getJSONObject(i)
                        var x=obj.getString("name")
                        if(x=="Attendance")
                        {
                            AttModID=obj.getString("instance")
                        }
                    }

                }
                callback(AttModID)
            }

        })
    }

}