package com.miniproject.attendx.course_details

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.Dashboard.RecyclerViewDashboard_Adapter
import com.miniproject.attendx.Dashboard.objDashboard
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.ActivitySessionPresentyDisplayingBinding
import com.miniproject.attendx.databinding.LoadingAlertDialogueBoxBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class session_presenty_displaying_activity : AppCompatActivity() {
    lateinit var binding:ActivitySessionPresentyDisplayingBinding
    lateinit var sessionId:String
    lateinit var sessionDate:String
    lateinit var courseId:String
    lateinit var courseName:String
    lateinit var presentStatusId:String
    lateinit var absentStatusId:String
    lateinit var jsonAttendanceLogs:JSONArray
    var dataStudNameAndStatus= arrayListOf<data_attendance_report_show_object>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySessionPresentyDisplayingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // status bar color here
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        sessionId= intent.getStringExtra("sessionid").toString()
        sessionDate= intent.getStringExtra("date").toString()
        courseId=intent.getStringExtra("courseid").toString()
        courseName=intent.getStringExtra("coursename").toString()
        binding.recordingAttendanceToolbarTextview.text="Session details for ${sessionDate}"

        var bindingx= LoadingAlertDialogueBoxBinding.inflate(layoutInflater)
        bindingx.loadingAlertDialogueBoxText.text="Fetching sessions details..."
        var x= AlertDialog.Builder(this)
            .setView(bindingx.root)
            .show()

        FetchSessionPresenty(sessionId){presentStatusId,absentstatusid->
            Log.d("jsonAttendanceLogs",jsonAttendanceLogs.toString())
            for (i in 0 until jsonAttendanceLogs.length()){
                var currentStatusID=jsonAttendanceLogs.getJSONObject(i).getString("statusid")
                var currentStudentId:String
                lateinit var presentOrAbsent:String
                if(currentStatusID==absentStatusId) {
                    presentOrAbsent="ABSENT"
                } else {
                    presentOrAbsent="PRESENT"
                }
                currentStudentId=jsonAttendanceLogs.getJSONObject(i).getString("studentid")
                getStudentNameFromStudentID(currentStudentId,courseId){fullname,noOfUser->
                    dataStudNameAndStatus.add(data_attendance_report_show_object(fullname,presentOrAbsent))
                    if(dataStudNameAndStatus.size.toString()==noOfUser)
                    {
                        x.dismiss()
                        runOnUiThread {
                            binding.sessionPresentyDisplayRecyclerView.adapter=presenty_showing_RecyclerView_adapter(dataStudNameAndStatus)
                        }
                    }
                }
            }
        }

    }

    fun FetchSessionPresenty(sessionId: String,callback: (String,String)->Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_get_session",
            "moodlewsrestformat" to "json",
            "sessionid" to sessionId
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

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val sessionObj = JSONObject(responseBody)
                    val statuses=sessionObj.getJSONArray("statuses")
                    jsonAttendanceLogs=sessionObj.getJSONArray("attendance_log")
                    Log.d("stafasfsaf",statuses.toString())
                    val statusObj=statuses.getJSONObject(0)
                    Log.d("stafasfsaf",statusObj.toString())
                    presentStatusId=statusObj.getString("id")
                    absentStatusId=(presentStatusId.toInt()+1).toString()
                }
                callback(presentStatusId,absentStatusId)
            }

        })
    }

    fun getStudentNameFromStudentID(StudentId: String, courseId: String,callback:(String,String)->Unit) {
            val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

            val params = mapOf(
                "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
                "wsfunction" to "core_enrol_get_enrolled_users",
                "moodlewsrestformat" to "json",
                "courseid" to courseId
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
                        val users = JSONArray(responseBody)
                        val noOfUser=JSONArray(responseBody).length()
                        for (i in 0 until users.length()){
                            val user=users.getJSONObject(i)
                            if(user.getString("id")==StudentId)
                            {
                                callback(user.getString("fullname"),noOfUser.toString())
                            }
                        }


                    }

                }
            })
        }
}