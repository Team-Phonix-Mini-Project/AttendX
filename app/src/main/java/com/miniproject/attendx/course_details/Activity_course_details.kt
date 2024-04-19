package com.miniproject.attendx.course_details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miniproject.attendx.R
import com.miniproject.attendx.attendance.AttendanceTakingActivity
import com.miniproject.attendx.attendance.attendance_module_list_object
import com.miniproject.attendx.databinding.ActivityCourseDetailsBinding
import com.miniproject.attendx.databinding.AttReportModuleNameListBinding

class activity_course_details : AppCompatActivity() {
    lateinit var binding: ActivityCourseDetailsBinding
    var dataModuleName = arrayListOf<attendance_module_list_object>()
    private lateinit var database: DatabaseReference
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

        database = FirebaseDatabase.getInstance().reference.child("AttendanceLogs")
        var courseID = intent.getStringExtra("courseid")
        var name = intent.getStringExtra("Name")
        var appl = intent.getStringExtra("User")
        appl = "Total applicants : " + appl
        binding.courseDetailsCourseName.text = name
        binding.courseDetailsApplicants.text = appl
        binding.courseDetailsTakeAttendance.setOnClickListener {
            onTakeAttendanceButtonClicked(courseID, name)
        }
        binding.courseDetailsGetAttendanceReport.setOnClickListener {
            onGetAttendanceReportClicked(courseID, name)
        }
    }

    private fun onTakeAttendanceButtonClicked(courseID: String?, name: String?) {
        var intent = Intent(this, AttendanceTakingActivity::class.java)
        intent.putExtra("Name", name)
        intent.putExtra("courseid", courseID)
        startActivity(intent)
    }

    private fun onGetAttendanceReportClicked(courseID: String?, courseName: String?) {
        ReadFromDatabase(courseID.toString()) { courseID ->
            Log.d("READFROMDATABSECOMPLETED", dataModuleName.toString())
            val dialogBinding = AttReportModuleNameListBinding.inflate(layoutInflater)
            val adapter = Report_module_list_RecyclerView_adapter(
                dataModuleName,
                object : AttendanceModuleClickListener_report {
                    override fun onAttendanceModuleClicked(attendanceModule: attendance_module_list_object) {
                        // Handle the click event here
                        val attendanceID = attendanceModule.attId
                        runOnUiThread {
                            Toast.makeText(
                                this@activity_course_details,
                                "Selected ${attendanceModule.attName}(id->$attendanceID) for attendance",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        AlertDialog.Builder(this@activity_course_details)
                            .setTitle("Are you sure you want to see report of module ${attendanceModule.attName}?")
                            .setPositiveButton("YES", { _, _ ->
                                runOnUiThread {
                                    Toast.makeText(
                                        this@activity_course_details,
                                        "Showing Report for ${attendanceModule.attName}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                var intent = Intent(
                                    this@activity_course_details,
                                    ReportOfAttendanceModuleSessionList::class.java
                                )
                                intent.putExtra("attname", attendanceModule.attName)
                                intent.putExtra("attid", attendanceModule.attId)
                                intent.putExtra("courseid",courseID)
                                intent.putExtra("coursename",courseName)
                                startActivity(intent)
                            })
                            .setNegativeButton("CANCEL", { _, _ ->

                            })
                            .show()
                    }
                })
            dialogBinding.attReportModuleNamesRecyclerView.adapter = adapter
            AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .show()
        }
    }

    private fun ReadFromDatabase(courseID: String, callback: (String) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (courseSnapshot in dataSnapshot.children) {
                    val courseIdCompare = courseSnapshot.key // Get the courseID form the FB

                    when (courseIdCompare) {
                        courseID -> {

                            for (moduleSnapshot in dataSnapshot.child("$courseID").children) {


                                val moduleName = moduleSnapshot.value // Retrieve the value

                                Log.d(
                                    "ModuleList",
                                    "$moduleName  ${moduleSnapshot.child("attendanceName").value}"
                                )
                                var x = moduleSnapshot.child("attendanceName").value
                                var y = moduleSnapshot.child("attendanceID").value
                                if (!dataModuleName.contains(
                                        attendance_module_list_object(
                                            x.toString(),
                                            y.toString()
                                        )
                                    )
                                ) {
                                    dataModuleName.add(
                                        attendance_module_list_object(
                                            x.toString(),
                                            y.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                callback(courseID)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}