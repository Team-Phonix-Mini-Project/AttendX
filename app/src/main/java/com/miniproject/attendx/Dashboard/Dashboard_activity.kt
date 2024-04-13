package com.miniproject.attendx.Dashboard
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.miniproject.attendx.databinding.ActivityDashboardBinding
import java.io.IOException

class Dashboard_activity : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding
    var data = arrayListOf<objDashboard>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FetchUserName()
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
                    val courses = JSONArray(responseBody)
                    for (i in 1 until courses.length()) {
                        val course = courses.getJSONObject(i)
                        val courseId=course.getString("id")
                        val courseName = course.getString("fullname")
                        Log.d("TAGXX",courseName)
                        Log.d("TAGXX",courseId)
                        FetchApplicantsList(courseId,courseName)
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
                    var applicant=users.length()
                    data.add(objDashboard(courseName,applicant.toString(),courseId))

                    runOnUiThread {
                        binding.RecyclerView.adapter=RecyclerViewDashboard_Adapter(data)
                    }
                }

            }
        })
    }

}