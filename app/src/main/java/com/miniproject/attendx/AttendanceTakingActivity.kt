//	BACKUP FOP ATTENDANCE ACTIVITY 14-4   12:19 AM


package com.miniproject.attendx

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.databinding.ActivityAttendanceTakingBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AttendanceTakingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceTakingBinding
    lateinit var attendanceID: String
    lateinit var sessionID:String
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

        binding.attendanceTakingCreateModule.setOnClickListener {
            toCreateAttendanceModule(courseID){attendanceModID->
              attendanceID=attendanceModID
                binding.attendanceTakingCreateSession.setOnClickListener {
                    createSessionForAttendance(attendanceID,courseID){sessionIdForMod->
                        sessionID=sessionIdForMod
                        makeUpdationsInAttendance(attendanceID,courseID,sessionID){studentID,studentName->
                            Log.d("TAGSTU","[CourseID="+courseID+"] "+"[attendID="+attendanceModID+"] "+"[sessiID="+sessionID+"] "+"[studentID="+studentID+"] "+"[studName="+studentName+"] ")
                        }
                    }
                }
            }
        }

    }

    private fun toCreateAttendanceModule(courseID: String, callback: (String) -> Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_add_attendance",
            "moodlewsrestformat" to "json",
            "courseid" to courseID,
            "name" to "Attendance_Success"
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
                    var attendanceModID=courses.getString("attendanceid")
                    callback(attendanceModID)
                }
            }

        })
    }
    private fun createSessionForAttendance(attendanceID: String, courseID: String,callback: (String) -> Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
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
                    var sessionIDForMod=data.getString("sessionid")
                    callback(sessionIDForMod)
                }
            }

        })
    }
    fun getCurrentUnixTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }
    private fun makeUpdationsInAttendance(attendanceID: String, courseID: String, sessionID: String,callback: (String,String) -> Unit) {
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
                    var i = 2
                    while (i < users.length()) {
                        val user = users.getJSONObject(i)
                        val studentID = user.getString("id")
                        val studentName = user.getString("fullname")
                        callback(studentID,studentName)
                        i++
                    }
                }

            }
        })
    }

    data class getStatusObj(val statusID:String,
                            val statusSet:String,
                            val studentID:String,
                            val takenByID:String,
                            val sessionID:String
    )
    private fun getStatusID(
        studentID: String,
        sessionID: String,
        courseID: String,
        studentName: String
    ){
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
                    var obj=JSONObject(responseBody)
                    var objs=obj.getJSONArray("statuses")
                    var x=objs.getJSONObject(0)
                    Log.d("StatusTag",x.getString("id"))
                    var statusIdPresent=x.getString("id")
                    var statusset=1;
                    var takenByid=studentID;

                    binding.attendanceTakingPresentBtn.setOnClickListener {
                        markAttendance(statusIdPresent,studentID,statusset,takenByid,sessionID)
                    }
                    binding.attendanceTakingAbsentBtn.setOnClickListener {
                        markAttendance(statusIdPresent+1,studentID,statusset,takenByid,sessionID)
                    }

                }

            }
        })

    }

    private fun markAttendance(
        statusIdPresent: String,
        studentID: String,
        statusset: Int,
        takenByid: String,
        sessionID: String
    ) {

        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_update_user_status",
            "moodlewsrestformat" to "json",
            "sessionid" to sessionID.toString(),
            "studentid" to studentID.toString(),
            "takenbyid" to takenByid.toString(),
            "statusid" to statusIdPresent.toString(),
            "statusset" to statusset.toString()
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

                    Log.d("TAGXX","T"+studentID)
                }
            }

        })

    }


}