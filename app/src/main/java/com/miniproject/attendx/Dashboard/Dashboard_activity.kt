package com.miniproject.attendx.Dashboard

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miniproject.attendx.databinding.ActivityDashboardBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class Dashboard_activity : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding
    var data = arrayListOf<objDashboard>()

    private lateinit var auth: FirebaseAuth

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FetchUserName()

        // Heere is the Firebase code
        auth = FirebaseAuth.getInstance()
//        val user = auth.currentUser
//        if (user == null) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        } else {
//            Toast.makeText(this, user.email, Toast.LENGTH_SHORT).show()
//        }

        // Add click listener to logout button
//        binding.logoutButton.setOnClickListener {
//            // Sign out the user
//            auth.signOut()
//
//            // Redirect to login activity
//            startActivity(Intent(this, LoginActivity::class.java).apply {
//                putExtra("fuckOFF", false)
//            })
//            finish()
//        }


        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference

        // Read data from the database
        readDataOnce()


    }

    fun FetchUserName() {
        val url = "https://attendancex.moodlecloud.com/webservice/rest/server.php"

        val params = mapOf(
            "wstoken" to "cd8c3e7ed7bf515ad9a3fec7f7f8e8ef",
            "wsfunction" to "core_course_get_courses",
            "moodlewsrestformat" to "json"
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
                    val courses = JSONArray(responseBody)
                    for (i in 1 until courses.length()) {
                        val course = courses.getJSONObject(i)
                        val courseId = course.getString("id")
                        val courseName = course.getString("fullname")
                        Log.d("TAGXX", courseName)
                        Log.d("TAGXX", courseId)
                        FetchApplicantsList(courseId, courseName)
                    }
                }
            }
        })
    }

    private fun FetchApplicantsList(courseId: String, courseName: String) {
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
                    var applicant = users.length()
                    data.add(objDashboard(courseName, applicant.toString(), courseId))

                    runOnUiThread {
                        binding.RecyclerView.adapter = RecyclerViewDashboard_Adapter(data)
                    }
                }

            }
        })
    }


    // Variables to store the Tokens of the course teacher to access the specific Course
    val token_cn = ""
    val token_se = ""
    val token_ps = ""
    val token_java = ""
    val token_ap = ""


    // UID for all teachers in Firebase
    val cn_UID = "8qd1AmvNspdMriLQmKGh1wvxZNm1"
    val ps_UID = "7zXfIyDi76dmKlzkjsGHvsLCVMQ2"
    val se_UID = "2S8Gy6tcwKQ8ABIqVJMA2ztthuR2"


    var CurrentToken: String = "" // Token retrived from Firebase after Authentication (Login)


    // Now compare the CurrentToken and the HardCoded Tokens
    // Here


    private fun readDataOnce() {
        // Add listener to database reference for a single value event
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value
                for (snapshot in dataSnapshot.children) {
                    // Iterate through each child node
                    val key = snapshot.key // Retrieve the key
                    val value = snapshot.value // Retrieve the value

                    when (key) {
                        cn_UID -> {
                            Toast.makeText(
                                applicationContext, "Key: $key, Value: $value", Toast.LENGTH_LONG
                            ).show()

                            CurrentToken = key


                        }

                        se_UID -> {
                            Toast.makeText(
                                applicationContext, "Key: $key, Value: $value", Toast.LENGTH_LONG
                            ).show()

                            CurrentToken = key

                        }

                        ps_UID -> {
                            Toast.makeText(
                                applicationContext, "Key: $key, Value: $value", Toast.LENGTH_LONG
                            ).show()

                            CurrentToken = key

                        }
                    }
                    // Do something with the retrieved data
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Toast.makeText(applicationContext, "Failed to read value.", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

}