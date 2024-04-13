package com.miniproject.attendx

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.Dashboard.RecyclerViewDashboard_Adapter
import com.miniproject.attendx.Dashboard.objDashboard
import com.miniproject.attendx.databinding.ActivityAttendanceTakingBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class AttendanceTakingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceTakingBinding
    private val data = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAttendanceTakingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.attendanceTakingCourseName.text =
            "Taking Attendance for " + intent.getStringExtra("Name")
        val courseID = intent.getStringExtra("courseid").toString()
        binding.attendanceTakingCreateSession.setOnClickListener {
            toCreateAttendance(courseID)
        }
    }

    private fun toCreateAttendance(courseID: String) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "5e62aa6a8c3ab1b1a3aefa9e39fad4be",
            "wsfunction" to "mod_attendance_add_attendance",
            "moodlewsrestformat" to "json",
            "courseid" to courseID,
            "name" to "AttendanceXYX"
        )
        val formBody = FormBody.Builder()
        for((key,value) in params)
        {
            formBody.add(key,value)
        }
        val request = Request.Builder()
            .url(url)
            .post(formBody.build())
            .build()

        val client=OkHttpClient()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val courses = JSONObject(responseBody)
                    var attendanceID=courses.getString("attendanceid")
                    var x="Attendance Id : "
                    x=x+attendanceID
                    Log.d("TAGR",x)
                    createSessionForAttendance(attendanceID,courseID)

                }
            }

        })

    }

    private fun createSessionForAttendance(attendanceID: String, courseID: String) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "5e62aa6a8c3ab1b1a3aefa9e39fad4be",
            "wsfunction" to "mod_attendance_add_session",
            "moodlewsrestformat" to "json",
            "attendanceid" to attendanceID,
            "sessiontime" to getCurrentUnixTimestamp().toString()
        )
        val formBody = FormBody.Builder()
        for((key,value) in params)
        {
            formBody.add(key,value)
        }
        val request = Request.Builder()
            .url(url)
            .post(formBody.build())
            .build()

        val client=OkHttpClient()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val data = JSONObject(responseBody)
                    var sessionID=data.getString("sessionid")
                    var x="Session Id : "
                    x=x+sessionID
                    Log.d("TAGR",x)
                    makeUpdationsInAttendance(attendanceID,courseID,sessionID)

                }
            }

        })
    }
    fun getCurrentUnixTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }

    private fun makeUpdationsInAttendance(attendanceID: String, courseID: String, sessionID: String) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "5e62aa6a8c3ab1b1a3aefa9e39fad4be",
            "wsfunction" to "core_enrol_get_enrolled_users",
            "moodlewsrestformat" to "json",
            "courseid" to courseID
        )

        val formBody = FormBody.Builder()
        for ((key, value) in params) {
            formBody.add(key, value)
        }

        val request = Request.Builder()
            .url(url)
            .post(formBody.build())
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val users = JSONArray(responseBody)
                    var i=1
                    while (i<users.length()){
                        val user = users.getJSONObject(i)
                        val studentID=user.getString("id")
                        val studentName = user.getString("fullname")
                        Log.d("TAGXX",studentName+ "Student id"+studentID+" Session id:"+sessionID+"Att id "+attendanceID)

                        i++


                    //NOW IMPLEMENT FOR UPDATING ATTENDANCE OF USER IF ABSENT I++ IF PRESENT I++ else it will remain on same name and id





                    }



                }

            }
        })
    }



}