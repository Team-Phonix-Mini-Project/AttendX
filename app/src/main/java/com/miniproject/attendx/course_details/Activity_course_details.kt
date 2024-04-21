package com.miniproject.attendx.course_details

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.R
import com.miniproject.attendx.attendance.MarkingAttDataObj
import com.miniproject.attendx.attendance.RecordingAttendance
import com.miniproject.attendx.databinding.ActivityCourseDetailsBinding
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class activity_course_details : AppCompatActivity() {
    lateinit var binding: ActivityCourseDetailsBinding
    lateinit var AttModID: String
    lateinit var courseName: String
    lateinit var sessionID: String
    lateinit var courseID: String
    var dataArray = arrayListOf<MarkingAttDataObj>()
    var TakeAttendanceClicked = false

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

        // status bar color here
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        courseID = intent.getStringExtra("courseid").toString()
        var courseID = intent.getStringExtra("courseid")
        var name = intent.getStringExtra("Name")
        courseName = name.toString()
        var appl = intent.getStringExtra("User")
        appl = "Total students : " + appl
        binding.courseDetailsCourseName.text = name
        binding.courseDetailsApplicants.text = appl
        binding.courseDetailsTakeAttendance.setOnClickListener {
            courseName = intent.getStringExtra("Name") ?: ""
            courseID = intent.getStringExtra("courseid") ?: ""
            if (TakeAttendanceClicked == true) {
                Toast.makeText(
                    this@activity_course_details,
                    "Creating session..wait",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (TakeAttendanceClicked == false) {
                TakeAttendanceClicked = true
                var bindingx: LoadingAlertDialogueBoxBinding
                bindingx = LoadingAlertDialogueBoxBinding.inflate(layoutInflater)
                bindingx.loadingAlertDialogueBoxText.text = "Creating session.."
                var x = MaterialAlertDialogBuilder(this)
                    .setView(bindingx.root)
                    .show()
                fetchAttendanceModuleID(courseID.toString()) { AttModID ->
                    createSessionForAttendance(AttModID, courseID.toString()) { sessionIDForMod ->
                        sessionID = sessionIDForMod
                        makeUpdationsInAttendance(
                            AttModID,
                            courseID.toString(),
                            sessionIDForMod
                        ) { studentID, studentName, noOfUsers ,role ->
                            Log.d(
                                "makeUpdationsInAttendance",
                                studentName + " " + studentID + " " + noOfUsers
                            )
                            if(role=="admin" || role=="teacher_se" || role=="teacher_cn" ||role=="teacher_ps"||role=="teacherjava"||role=="teachertoc" ||role=="teacherps")
                            {

                            }
                            else
                            {
                                getStatusID(
                                    studentID,
                                    sessionIDForMod,
                                    courseID.toString(),
                                    studentName
                                ) { statusIdPresent, statusset, takenByid ->
                                    Log.d(
                                        "getStatusID",
                                        "statusid:$statusIdPresent studID:$studentID studName:$studentName"
                                    )
                                    val obj = MarkingAttDataObj(
                                        courseID.toString(),
                                        AttModID,
                                        sessionID,
                                        studentName,
                                        studentID,
                                        statusIdPresent
                                    )
                                    dataArray.add(obj)
                                    Log.d("MarkingAttDataObj", obj.toString())
                                    Log.d("noOfUsers", noOfUsers + "=" + dataArray.size)
                                    if (noOfUsers.toString() == dataArray.size.toString()) {
                                        Log.d("noOfUsers", "If condition satisfied")
                                        x.dismiss()
                                        runOnUiThread {
                                            var intent = Intent(
                                                this,
                                                RecordingAttendance::class.java
                                            )
                                            dataArray.sortBy { it.studentName.split(" ").first() }
                                            Log.d("studentName__",dataArray.toString())
                                            intent.putExtra("data", dataArray)
                                            intent.putExtra("coursename", courseName)
                                            startActivity(intent)

                                        }
                                    }
                                }
                            }

                        }
                    }

                }
            }
        }
        binding.courseDetailsGetAttendanceReport.setOnClickListener {
            onGetAttendanceReportClicked(courseID, name)
        }
    }


    private fun onGetAttendanceReportClicked(courseID: String?, courseName: String?) {
        var bindingx = LoadingAlertDialogueBoxBinding.inflate(layoutInflater)
        bindingx.loadingAlertDialogueBoxText.text = "Getting report..."
        var x = MaterialAlertDialogBuilder(this)
            .setView(bindingx.root)
            .show()
        fetchAttendanceModuleID(courseID.toString()) { attendanceMod ->
            AttModID = attendanceMod
            x.dismiss()
            var intent = Intent(this, ReportOfAttendanceModuleSessionList::class.java)
            intent.putExtra("attname", "Attendance")
            intent.putExtra("attid", AttModID)
            intent.putExtra("courseid", courseID)
            intent.putExtra("coursename", courseName)
            startActivity(intent)
        }
    }


    private fun fetchAttendanceModuleID(courseID: String, callback: (String) -> Unit) {
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
                    val data = datas.getJSONObject(0)
                    var array = data.getJSONArray("modules")
                    for (i in 0 until array.length()) {
                        var obj = array.getJSONObject(i)
                        var x = obj.getString("name")
                        if (x == "Attendance") {
                            AttModID = obj.getString("instance")
                        }
                    }
                }
                callback(AttModID)
            }

        })
    }


    private fun createSessionForAttendance(
        attendanceID: String, courseID: String, callback: (String) -> Unit
    ) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_add_session",
            "moodlewsrestformat" to "json",
            "attendanceid" to attendanceID,
            "sessiontime" to getCurrentUnixTimestamp().toString()
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
                    val data = JSONObject(responseBody)
                    var sessionIDForMod = data.getString("sessionid")
                    callback(sessionIDForMod)
                }
            }

        })
    }

    fun getCurrentUnixTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUnixTime(unixTime: Long): String {
        val instant = Instant.ofEpochSecond(unixTime)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateTime.format(formatter)
    }

    private fun makeUpdationsInAttendance(
        attendanceID: String,
        courseID: String,
        sessionID: String,
        callback: (String, String, String,String) -> Unit
    ) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "core_enrol_get_enrolled_users",
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
                    val users = JSONArray(responseBody)
                    val noOfUsers = (users.length()-2).toString()
                    var i = 0
                    while (i < users.length()) {
                        val user = users.getJSONObject(i)
                        val studentID = user.getString("id")
                        val role=user.getString("username")
                        val studentName = user.getString("fullname")
                        callback(studentID, studentName, noOfUsers.toString(),role)
                        i++
                    }
                }

            }
        })
    }

    private fun getStatusID(
        studentID: String,
        sessionID: String,
        courseID: String,
        studentName: String,
        callback: (String, String, String) -> Unit
    ) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_get_session",
            "moodlewsrestformat" to "json",
            "sessionid" to sessionID
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
                    var obj = JSONObject(responseBody)
                    var objs = obj.getJSONArray("statuses")
                    var x = objs.getJSONObject(0)
                    var statusIdPresent = x.getString("id")
                    var statusset = "1";
                    var takenByid = studentID;
                    callback(statusIdPresent, statusset, takenByid)
                }
            }
        })
    }


}