package com.miniproject.attendx.course_details

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.ActivityReportOfAttendanceModuleBinding
import com.miniproject.attendx.databinding.LoadingAlertDialogueBoxBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReportOfAttendanceModuleSessionList : AppCompatActivity() {
    lateinit var binding: ActivityReportOfAttendanceModuleBinding
    lateinit var attendanceName: String
    lateinit var attendanceId: String
    lateinit var courseId: String
    lateinit var courseName: String
    var sessionList = arrayListOf<sessionIdAndNameData_Object>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("ReportOfAttendanceModule", "ReportOfAttendanceModule")

        binding = ActivityReportOfAttendanceModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // status bar color here
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)


        attendanceName = intent.getStringExtra("attname").toString()
        attendanceId = intent.getStringExtra("attid").toString()
        courseId = intent.getStringExtra("courseid").toString()
        courseName = intent.getStringExtra("coursename").toString()
        binding.recordingAttendanceToolbarTextview.text = "Session conducted for ${courseName}"
        var bindingx: LoadingAlertDialogueBoxBinding
        bindingx = LoadingAlertDialogueBoxBinding.inflate(layoutInflater)
        bindingx.loadingAlertDialogueBoxText.text = "Fetching sessions list..."
        var x = MaterialAlertDialogBuilder(this)
            .setView(bindingx.root)
            .show()
        FetchSessionsList(attendanceId, attendanceName, courseId, courseName) { sessionList ->
            runOnUiThread {
                x.dismiss()
                //Toast.makeText(this, sessionList.toString(), Toast.LENGTH_SHORT).show()
                if (sessionList.size == 0) {
                    var EmptySessionList = arrayListOf<sessionIdAndNameData_Object>(
                        sessionIdAndNameData_Object(
                            "not",
                            "not",
                            "not",
                            "not"
                        )
                    )
                    binding.reportSessionListRecyclerView.adapter =
                        report_session_list_adapter(EmptySessionList)
                } else {
                    binding.reportSessionListRecyclerView.adapter =
                        report_session_list_adapter(sessionList)
                }
            }
        }
    }

    fun FetchSessionsList(
        attendanceId: String,
        attendanceName: String,
        courseID: String,
        courseName: String,
        callback: (ArrayList<sessionIdAndNameData_Object>) -> Unit
    ) {
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

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val courses = JSONArray(responseBody)
                    for (i in 0 until courses.length()) {
                        val course = courses.getJSONObject(i)
                        val sesssionIdCurrentFetch = course.getString("id")
                        val sessionDate = convertUnixTime(course.getString("sessdate").toLong())
                        sessionList.add(
                            sessionIdAndNameData_Object(
                                sesssionIdCurrentFetch,
                                sessionDate,
                                courseID,
                                courseName
                            )
                        )
                    }

                }
                callback(sessionList)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUnixTime(unixTime: Long): String {
        val instant = Instant.ofEpochSecond(unixTime)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd    HH:mm:ss")
        return dateTime.format(formatter)
    }
}