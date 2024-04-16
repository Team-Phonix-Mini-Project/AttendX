//	BACKUP FOP ATTENDANCE ACTIVITY 14-4   4:30 pm

package com.miniproject.attendx.attendance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.ActivityAttendanceTakingBinding
import com.miniproject.attendx.databinding.AlertDialogueAttendanceNameBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AttendanceTakingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceTakingBinding
    lateinit var attendanceID: String
    lateinit var sessionID:String
    lateinit var courseName:String
    var dataArray= arrayListOf<MarkingAttDataObj>()
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
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
        courseName= intent.getStringExtra("Name").toString()
        val courseID = intent.getStringExtra("courseid").toString()

        binding.attendanceTakingCreateModule.setOnClickListener {
            var bindingX:AlertDialogueAttendanceNameBinding
            bindingX= AlertDialogueAttendanceNameBinding.inflate(layoutInflater)
            MaterialAlertDialogBuilder(this)
                .setView(bindingX.root)
                .setTitle("Attendance module name ?")
                .setPositiveButton("CREATE"){_,_->
                    var attendanceName=bindingX.alertDialogueAttendanceName.text.toString()
                    Log.d("CHECKTAGS",attendanceName)
                    if(attendanceName!=null)
                    {
                        Log.d("CHECKTAGS","->"+attendanceName)

                        toCreateAttendanceModule(courseID,attendanceName){attendanceModID,attendanceName->
                            attendanceID=attendanceModID
                            runOnUiThread {
                                Toast.makeText(this@AttendanceTakingActivity,"Created attendance module ${attendanceName}",Toast.LENGTH_SHORT).show()
                            }
                            binding.attendanceTakingCreateSession.setOnClickListener {
                                createSessionForAttendance(attendanceID,courseID){sessionIdForMod->
                                    sessionID=sessionIdForMod
                                    runOnUiThread {
                                        Toast.makeText(this@AttendanceTakingActivity,"Session created",Toast.LENGTH_SHORT).show()
                                    }
                                    makeUpdationsInAttendance(attendanceID,courseID,sessionID){studentID,studentName,noOfUsers->
                                        getStatusID(studentID,sessionID,courseID,studentName){statusIdPresent,statusset,takenByid->
                                            Log.d("TAGSTATUS","[CourseID="+courseID+"] "+"[attendID="+attendanceModID+"] "+"[sessiID="+sessionID+"] "+"[studentID="+studentID+"] "+"[studName="+studentName+"] "+"[statusID=${statusIdPresent}]")

                                            val obj= MarkingAttDataObj(courseID,attendanceID,sessionID,studentName,studentID,statusIdPresent)
                                            dataArray.add(obj)
                                            Log.d("TAGCURRENT",noOfUsers)
                                            var checker=(dataArray.size);
                                            Log.d("TAGCURRENT","-c "+checker.toString())
                                            if(checker.toString()==noOfUsers)
                                            {
                                                binding.attendanceTakingTakeAttendance.setOnClickListener {
                                                    Log.d("TAGCURRENT","IN_INTENT")
                                                    var intent=Intent(this, RecordingAttendance::class.java)
                                                    intent.putExtra("data",dataArray)
                                                    intent.putExtra("coursename",courseName)
                                                    startActivity(intent)
                                                }
                                            }
                                            else{
                                                binding.attendanceTakingTakeAttendance.setOnClickListener {
                                                    runOnUiThread {
                                                        Toast.makeText(this@AttendanceTakingActivity,"Wait",Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }

                }
                .setNegativeButton("CANCEL"){_,_->

                }
                .show()

        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun toCreateAttendanceModule(courseID: String,attendanceName:String,callback: (String, String) -> Unit) {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
        //var attendanceName="Attendance_Mod(${convertUnixTime(getCurrentUnixTimestamp())})"
        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "mod_attendance_add_attendance",
            "moodlewsrestformat" to "json",
            "courseid" to courseID,
            "name" to attendanceName
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
                    callback(attendanceModID,attendanceName)
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUnixTime(unixTime: Long): String {
        val instant = Instant.ofEpochSecond(unixTime)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateTime.format(formatter)
    }
    private fun makeUpdationsInAttendance(attendanceID: String, courseID: String, sessionID: String,callback: (String,String,String) -> Unit) {
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
                    val noOfUsers=users.length().toString()
                    var i = 0
                    while (i < users.length()) {
                        val user = users.getJSONObject(i)
                        val studentID = user.getString("id")
                        val studentName = user.getString("fullname")
                        callback(studentID,studentName,noOfUsers.toString())
                        i++
                    }
                }

            }
        })
    }

    private fun getStatusID(studentID: String, sessionID: String, courseID: String, studentName: String,callback: (String, String,String) -> Unit)
    {
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
                    var statusset="1";
                    var takenByid=studentID;
                    callback(statusIdPresent,statusset,takenByid)
                }
            }
        })
    }

}