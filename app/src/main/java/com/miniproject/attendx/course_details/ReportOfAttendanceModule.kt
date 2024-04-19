package com.miniproject.attendx.course_details

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.ActivityReportOfAttendanceModuleBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class ReportOfAttendanceModule : AppCompatActivity() {
    lateinit var binding:ActivityReportOfAttendanceModuleBinding
    lateinit var attendanceName:String
    lateinit var attendanceId:String
    var sessionList= arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("ReportOfAttendanceModule", "ReportOfAttendanceModule")

        binding= ActivityReportOfAttendanceModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        attendanceName=intent.getStringExtra("attname").toString()
        attendanceId=intent.getStringExtra("attid").toString()
        FetchSessionsList(attendanceId,attendanceName){sessionList->

            runOnUiThread {
                Toast.makeText(this, sessionList.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun FetchSessionsList(attendanceId: String, attendanceName: String,callback: (ArrayList<String>)->Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_get_sessions",
            "moodlewsrestformat" to "json",
            "attendanceid" to attendanceId
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
                    val courses = JSONArray(responseBody)
                    for (i in 0 until courses.length()){
                        val course=courses.getJSONObject(i)
                        val sesssionIdCurrentFetch=course.getString("id")
                        sessionList.add(sesssionIdCurrentFetch)
                    }

                }
                callback(sessionList)
            }
        })
    }
}