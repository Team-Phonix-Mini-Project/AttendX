package com.miniproject.attendx.submitAttendance

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miniproject.attendx.R
import com.miniproject.attendx.attendance.markedDataObj
import com.miniproject.attendx.course_details.activity_course_details
import com.miniproject.attendx.databinding.ActivitySubmitAttendanceBinding

interface TextUpdateListener {
    fun updateText(presentUpdateNumber: String,absentUpdateNumber:String)
}

class SubmitAttendanceActivity : AppCompatActivity() ,TextUpdateListener{
    lateinit var binding: ActivitySubmitAttendanceBinding
    var data = arrayListOf<markedDataObj>()
    lateinit var totalPresentStudents:String
    lateinit var totalAbsentStudent:String
    lateinit var courseId: String
    lateinit var noOfUsers: String
    lateinit var courseName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySubmitAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        data = intent.getSerializableExtra("report") as ArrayList<markedDataObj>
        binding.submitAttendanceToolbarTextview.text =
            "Attendance report for " + intent.getStringExtra("coursename")

        courseName = intent.getStringExtra("coursename").toString()
        courseId = intent.getStringExtra("courseid").toString()
        noOfUsers = intent.getStringExtra("user").toString()
        totalPresentStudents=intent.getStringExtra("presentnumber").toString()
        totalAbsentStudent=intent.getStringExtra("absentnumber").toString()
        Log.d("totalPresentStudents","P->"+totalPresentStudents+"A->"+totalAbsentStudent)
        binding.submitAttendanceTotalAbsentNumber.text=totalAbsentStudent
        binding.submitAttendanceTotalPresentNumber.text=totalPresentStudents

        binding.submitAttendanceRecyclerView.adapter = submit_attendance_recycleView_adapter(data,this)
        binding.submitAttendanceButton.setOnClickListener {
            MaterialAlertDialogBuilder(this).setTitle("Are you sure you want to submit the attendance?")
                .setPositiveButton("YES") { _, _ ->
                    // Show a progress dialog while uploading attendance
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Uploading attendance...")
                    progressDialog.setCancelable(false) // Prevent dismissing by tapping outside
                    progressDialog.show()

                    // Simulate attendance upload process with a delay (replace with actual upload logic)
                    Handler().postDelayed({
                        // Dismiss the progress dialog after attendance is uploaded
                        progressDialog.dismiss()

                        // Show a success message or animation indicating successful upload
                        Toast.makeText(this, "Attendance uploaded successfully", Toast.LENGTH_SHORT)
                            .show()

                        // Navigate to the dashboard activity
                        var intent = Intent(this, activity_course_details::class.java)

                        intent.putExtra("courseid", courseId)
                        intent.putExtra("User", noOfUsers)
                        intent.putExtra("Name", courseName)
                        startActivity(intent)
                    }, 2000) // Adjust delay as needed (2 seconds in this example)


//                    var intent = Intent(this, activity_course_details::class.java)

//                    startActivity(intent)

                }.setNegativeButton("CANCEL") { _, _ ->

                }.show()

        }


    }


    override fun onBackPressed() {
        super.onBackPressedDispatcher

    }

    override fun updateText(presentUpdateNumber: String,absentUpdateNumber:String) {
        if(presentUpdateNumber=="inc" && absentUpdateNumber=="dec")
        {
            var a=totalAbsentStudent.toInt()
            a--
            totalAbsentStudent=a.toString()
            var p=totalPresentStudents.toInt()
            p++
            totalPresentStudents=p.toString()
        }
        else if(presentUpdateNumber=="dec" && absentUpdateNumber=="inc")
        {
            var a=totalAbsentStudent.toInt()
            a++
            totalAbsentStudent=a.toString()
            var p=totalPresentStudents.toInt()
            p--
            totalPresentStudents=p.toString()
        }
        runOnUiThread {
            updatePresentAbsentNumber()
        }
    }
    fun updatePresentAbsentNumber()
    {
        binding.submitAttendanceTotalAbsentNumber.text=totalAbsentStudent.toString()
        binding.submitAttendanceTotalPresentNumber.text=totalPresentStudents.toString()
    }
}