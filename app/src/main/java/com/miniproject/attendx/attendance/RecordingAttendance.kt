package com.miniproject.attendx.attendance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.miniproject.attendx.R
import com.miniproject.attendx.databinding.ActivityRecordingAttendanceBinding
import com.miniproject.attendx.submitAttendance.SubmitAttendanceActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class RecordingAttendance : AppCompatActivity() {
    lateinit var binding: ActivityRecordingAttendanceBinding
    var dataArray = arrayListOf<MarkingAttDataObj>()
    var dataMarkedArray = arrayListOf<markedDataObj>()
    lateinit var courseName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingAttendanceBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        dataArray = intent.getSerializableExtra("data") as ArrayList<MarkingAttDataObj>
        binding.recordingAttendanceToolbarTextview.text =
            "Recording Attendance for " + intent.getStringExtra("coursename")
        courseName = intent.getStringExtra("coursename").toString()
        binding.attendanceTakingGoToMainBtn.visibility = View.GONE
        var i = 0
        binding.recordingAttendanceStudentName.text = dataArray[0].studentName

        binding.attendanceTakingPresentBtn.setOnClickListener {
            if (binding.recordingAttendanceStudentName.text != "Attendance completed") {
                markAttendance(
                    dataArray[i].statusID,
                    dataArray[i].studentID,
                    1,
                    dataArray[i].studentID,
                    dataArray[i].sessionID
                )
                var obj = markedDataObj(
                    dataArray[i].statusID,
                    dataArray[i].studentID,
                    dataArray[i].statusID,
                    dataArray[i].sessionID,
                    dataArray[i].studentName,
                    "PRESENT"
                )
                dataMarkedArray.add(obj)
                if ((i + 1) < dataArray.size) {
                    binding.recordingAttendanceStudentName.text = dataArray[i + 1].studentName
                } else {
                    binding.recordingAttendanceStudentName.text = "Attendance completed"
                    i--
                    updateButtonVisibility()
                }
                i++
            }
        }

        binding.attendanceTakingAbsentBtn.setOnClickListener {
            if (binding.recordingAttendanceStudentName.text != "Attendance completed") {
                markAttendance(
                    ((dataArray[i].statusID).toInt() + 1).toString(),
                    dataArray[i].studentID,
                    1,
                    dataArray[i].studentID,
                    dataArray[i].sessionID
                )
                var obj = markedDataObj(
                    ((dataArray[i].statusID).toInt() + 1).toString(),
                    dataArray[i].studentID,
                    dataArray[i].statusID,
                    dataArray[i].sessionID,
                    dataArray[i].studentName,
                    "ABSENT"
                )
                dataMarkedArray.add(obj)
                if ((i + 1) < dataArray.size) {
                    binding.recordingAttendanceStudentName.text = dataArray[i + 1].studentName
                } else {
                    binding.recordingAttendanceStudentName.text = "Attendance completed"
                    i--
                    updateButtonVisibility()
                }
                i++
            }
        }


        binding.attendanceTakingGoToMainBtn.setOnClickListener {
            var intentX = Intent(this, SubmitAttendanceActivity::class.java)
            intentX.putExtra("report", dataMarkedArray)
            intentX.putExtra("coursename", courseName)
            startActivity(intentX)
        }

    }

    private fun markAttendance(
        statusIdPresent: String,
        studentID: String,
        statusset: Int,
        takenByid: String,
        sessionID: String
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"
            val params = mapOf(
                "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
                "wsfunction" to "mod_attendance_update_user_status",
                "moodlewsrestformat" to "json",
                "sessionid" to sessionID,
                "studentid" to studentID,
                "takenbyid" to takenByid,
                "statusid" to statusIdPresent,
                "statusset" to statusset.toString()
            )
            val formBody = FormBody.Builder()
            params.forEach { (key, value) ->
                formBody.add(key, value)
            }
            val request = Request.Builder()
                .url(url)
                .post(formBody.build())
                .build()

            try {
                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("markAttendance", "Failed to mark attendance for student $studentID")
                }
            } catch (e: IOException) {
                Log.e("markAttendance", "Network error: ${e.message}")
            }
        }
    }

    fun updateButtonVisibility() {
        Log.d("VisibilityCheck", "Text: ${binding.recordingAttendanceStudentName.text}")
        if (binding.recordingAttendanceStudentName.text == "Attendance completed") {
            binding.attendanceTakingGoToMainBtn.visibility = View.VISIBLE
        }
    }

}
