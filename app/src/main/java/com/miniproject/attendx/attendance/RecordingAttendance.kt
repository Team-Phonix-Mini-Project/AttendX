package com.miniproject.attendx.attendance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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
    lateinit var courseID: String
    lateinit var noOfUsers: String
    var noOfPresentStudents=0
    var noOfAbsentStudents=0

    // Audio
    private var mediaPlayerAbsent: MediaPlayer? = null
    private var mediaPlayerPresent: MediaPlayer? = null

    // vibrator
    private lateinit var vibrator: Vibrator


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

        // Audio for buttons
        mediaPlayerAbsent = MediaPlayer.create(this, R.raw.absent)
        mediaPlayerPresent = MediaPlayer.create(this, R.raw.present)

        // Vibrator
        // Initialize Vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        binding.recordingAttendanceAbsentNumber.text=noOfAbsentStudents.toString()
        binding.recordingAttendancePresentNumber.text=noOfPresentStudents.toString()
        // Status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        dataArray = intent.getSerializableExtra("data") as ArrayList<MarkingAttDataObj>
        binding.recordingAttendanceToolbarTextview.text =
            "Recording Attendance for " + intent.getStringExtra("coursename")
        courseName = intent.getStringExtra("coursename").toString()
        courseID = intent.getStringExtra("courseid").toString()
        noOfUsers = intent.getStringExtra("user").toString()
        binding.attendanceTakingGoToMainBtn.visibility = View.GONE
        var i = 0
        binding.recordingAttendanceStudentName.text = dataArray[0].studentName
        if(dataArray[0].Temp_array[0]=="PRESENT") { binding.recordingAttendancePrev1Status.setImageResource(R.drawable.present_symbol) }
        else if(dataArray[0].Temp_array[0]=="ABSENT"){ binding.recordingAttendancePrev1Status.setImageResource(R.drawable.absent_symbol) }
        else if(dataArray[0].Temp_array[0]=="SESSION_EMPTY"){binding.recordingAttendancePrev1Status.setImageResource(R.drawable.null_symbol)}

        if(dataArray[0].Temp_array[1]=="PRESENT") { binding.recordingAttendancePrev2Status.setImageResource(R.drawable.present_symbol) }
        else if(dataArray[0].Temp_array[1]=="ABSENT"){ binding.recordingAttendancePrev2Status.setImageResource(R.drawable.absent_symbol) }
        else if(dataArray[0].Temp_array[1]=="SESSION_EMPTY"){binding.recordingAttendancePrev2Status.setImageResource(R.drawable.null_symbol)}

        if(dataArray[0].Temp_array[2]=="PRESENT") { binding.recordingAttendancePrev3Status.setImageResource(R.drawable.present_symbol) }
        else if(dataArray[0].Temp_array[2]=="ABSENT"){ binding.recordingAttendancePrev3Status.setImageResource(R.drawable.absent_symbol) }
        else if(dataArray[0].Temp_array[2]=="SESSION_EMPTY"){binding.recordingAttendancePrev3Status.setImageResource(R.drawable.null_symbol)}

        if(dataArray[0].Temp_array[3]=="PRESENT") { binding.recordingAttendancePrev4Status.setImageResource(R.drawable.present_symbol) }
        else if(dataArray[0].Temp_array[3]=="ABSENT"){ binding.recordingAttendancePrev4Status.setImageResource(R.drawable.absent_symbol) }
        else if(dataArray[0].Temp_array[3]=="SESSION_EMPTY"){binding.recordingAttendancePrev4Status.setImageResource(R.drawable.null_symbol)}

        if(dataArray[0].Temp_array[4]=="PRESENT") { binding.recordingAttendancePrev5Status.setImageResource(R.drawable.present_symbol) }
        else if(dataArray[0].Temp_array[4]=="ABSENT"){ binding.recordingAttendancePrev5Status.setImageResource(R.drawable.absent_symbol) }
        else if(dataArray[0].Temp_array[4]=="SESSION_EMPTY"){binding.recordingAttendancePrev5Status.setImageResource(R.drawable.null_symbol)}



        binding.attendanceTakingPresentBtn.setOnClickListener {
            vibrate(50)
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
                noOfPresentStudents++
                updatePresentAbsentNumber()

                for (i in 0 until dataArray.size) {
                    Log.d("getSerializableExtra_data",dataArray[i].Temp_array.toString())
                }

                if ((i + 1) < dataArray.size) {
                    binding.recordingAttendanceStudentName.text = dataArray[i + 1].studentName
                    if(dataArray[i+1].Temp_array[0]=="PRESENT") { binding.recordingAttendancePrev1Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[0]=="ABSENT"){ binding.recordingAttendancePrev1Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[0]=="SESSION_EMPTY"){ binding.recordingAttendancePrev1Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[1]=="PRESENT") { binding.recordingAttendancePrev2Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[1]=="ABSENT"){ binding.recordingAttendancePrev2Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[1]=="SESSION_EMPTY"){ binding.recordingAttendancePrev2Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[2]=="PRESENT") { binding.recordingAttendancePrev3Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[2]=="ABSENT"){ binding.recordingAttendancePrev3Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[2]=="SESSION_EMPTY"){ binding.recordingAttendancePrev3Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[3]=="PRESENT") { binding.recordingAttendancePrev4Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[3]=="ABSENT"){ binding.recordingAttendancePrev4Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[3]=="SESSION_EMPTY"){ binding.recordingAttendancePrev4Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[4]=="PRESENT") { binding.recordingAttendancePrev5Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[4]=="ABSENT"){ binding.recordingAttendancePrev5Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[4]=="SESSION_EMPTY"){ binding.recordingAttendancePrev5Status.setImageResource(R.drawable.null_symbol)}
                }
                else {
                    binding.recordingAttendanceStudentName.text = "Attendance completed"
                    binding.recordingAttendancePrev1Status.visibility=View.GONE
                    binding.recordingAttendancePrev2Status.visibility=View.GONE
                    binding.recordingAttendancePrev3Status.visibility=View.GONE
                    binding.recordingAttendancePrev4Status.visibility=View.GONE
                    binding.recordingAttendancePrev5Status.visibility=View.GONE
                    i--
                    updateButtonVisibility()
                }
                i++
            }
        }

        binding.attendanceTakingAbsentBtn.setOnClickListener {

            // Audio feature
//            mediaPlayerAbsent?.start()
            vibrate(50)
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
                noOfAbsentStudents++
                updatePresentAbsentNumber()
                if ((i + 1) < dataArray.size) {
                    binding.recordingAttendanceStudentName.text = dataArray[i + 1].studentName
                    if(dataArray[i+1].Temp_array[0]=="PRESENT") { binding.recordingAttendancePrev1Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[0]=="ABSENT"){ binding.recordingAttendancePrev1Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[0]=="SESSION_EMPTY"){ binding.recordingAttendancePrev1Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[1]=="PRESENT") { binding.recordingAttendancePrev2Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[1]=="ABSENT"){ binding.recordingAttendancePrev2Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[1]=="SESSION_EMPTY"){ binding.recordingAttendancePrev2Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[2]=="PRESENT") { binding.recordingAttendancePrev3Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[2]=="ABSENT"){ binding.recordingAttendancePrev3Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[2]=="SESSION_EMPTY"){ binding.recordingAttendancePrev3Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[3]=="PRESENT") { binding.recordingAttendancePrev4Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[3]=="ABSENT"){ binding.recordingAttendancePrev4Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[3]=="SESSION_EMPTY"){ binding.recordingAttendancePrev4Status.setImageResource(R.drawable.null_symbol)}

                    if(dataArray[i+1].Temp_array[4]=="PRESENT") { binding.recordingAttendancePrev5Status.setImageResource(R.drawable.present_symbol) }
                    else if(dataArray[i+1].Temp_array[4]=="ABSENT"){ binding.recordingAttendancePrev5Status.setImageResource(R.drawable.absent_symbol) }
                    else if(dataArray[i+1].Temp_array[4]=="SESSION_EMPTY"){ binding.recordingAttendancePrev5Status.setImageResource(R.drawable.null_symbol)}
                } else {
                    binding.recordingAttendanceStudentName.text = "Attendance completed"
                    binding.recordingAttendancePrev1Status.visibility=View.GONE
                    binding.recordingAttendancePrev2Status.visibility=View.GONE
                    binding.recordingAttendancePrev3Status.visibility=View.GONE
                    binding.recordingAttendancePrev4Status.visibility=View.GONE
                    binding.recordingAttendancePrev5Status.visibility=View.GONE
                    i--
                    updateButtonVisibility()
                }
                i++
            }
        }


        binding.attendanceTakingGoToMainBtn.setOnClickListener {
            Log.d("noOfPAStudents","A: $noOfAbsentStudents P: $noOfPresentStudents")
            var intentX = Intent(this, SubmitAttendanceActivity::class.java)
            intentX.putExtra("report", dataMarkedArray)
            intentX.putExtra("coursename", courseName)
            intentX.putExtra("courseid", courseID)
            intentX.putExtra("user", noOfUsers)
            intentX.putExtra("presentnumber",noOfPresentStudents.toString())
            intentX.putExtra("absentnumber",noOfAbsentStudents.toString())
            startActivity(intentX)
            vibrate(100)
        }


    }

    fun updatePresentAbsentNumber()
    {
        binding.recordingAttendanceAbsentNumber.text=noOfAbsentStudents.toString()
        binding.recordingAttendancePresentNumber.text=noOfPresentStudents.toString()
    }


    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when the activity is destroyed
        mediaPlayerPresent?.release()
        mediaPlayerPresent = null
        mediaPlayerAbsent?.release()
        mediaPlayerAbsent = null
    }

    // vibrator
    private fun vibrate(duration: Long) {
        // Check if the device has a vibrator
        if (vibrator.hasVibrator()) {
            // Vibrate with the specified duration
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(duration)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        super.onBackPressedDispatcher

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
            binding.attendanceTakingPresentBtn.visibility = View.GONE
            binding.attendanceTakingAbsentBtn.visibility = View.GONE
        }
    }

}