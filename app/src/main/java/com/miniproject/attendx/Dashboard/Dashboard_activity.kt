package com.miniproject.attendx.Dashboard

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miniproject.attendx.Login_activity.LoginActivity
import com.miniproject.attendx.R
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

    private lateinit var sharedPreferences: SharedPreferences


    private lateinit var database: DatabaseReference
    private lateinit var infoReference: DatabaseReference


    private lateinit var userEmail: String
    private lateinit var userUID: String
    lateinit var currentTOKEN: String

    // For firebase retrieval
    lateinit var uid: String
    lateinit var courseName: String
    lateinit var token: String


    // Search init
    var flag: Boolean = false

    var courseNameToTokenMap = mutableMapOf<String, String>()


    // Declarations for the navigation pane
    private var isNavigationVisible =
        false // Initially set to false assuming the navigation pane is hidden
//    val rect = Rect()

//    lateinit var courseName: String
//    lateinit var TOKEN: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // status bar color here
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)

        // Heere is the Firebase code
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser // Retrieves the current user from Firebase Authentication

        userEmail = user?.email!!
        userUID = user.uid
//        if (user == null) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        } else {
//            Toast.makeText(this, user.email, Toast.LENGTH_SHORT).show()
//        }

//         Add click listener to logout button
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        // Important


        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference
        infoReference = FirebaseDatabase.getInstance().reference.child("uidInfo")


        // Read data from the database
        readDataOnce() { courseNameToTokenMap ->
            FetchUserName()
        }


//        WriteOntoDatabase(user)


        // Onclick for Navigation Pane

        binding.navigationPane.visibility = View.GONE

        setupNavigationPane()
        binding.drawerIconId.setOnClickListener {
            openNavigationPane()
        }
        binding.overlay.setOnClickListener {
            closeNavigationPane()
        }
        binding.buttonAboutUs.setOnClickListener {
            val builder = AlertDialog.Builder(this)



            // Inflate the custom layout for the dialog content
            val dialogView = layoutInflater.inflate(R.layout.about_us_layout, null)
            builder.setView(dialogView)
            // Create the AlertDialog object
            val dialog: AlertDialog = builder.create()

            // Show the dialog
            dialog.show()

        }

        // Setting the User == Email dynamic

        binding.textViewHiUser.setText("Hi $userEmail!")
        binding.userEmailNavPane.setText(userEmail)


        // Searching Functionality
        binding.searchButton.setOnClickListener {
            showEditTextDialog()
        }


    }

    private fun LogoutFunction() {
        // Sign out the user
        auth.signOut()

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("name", "")
        editor.apply()

        // Redirect to login activity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showLogoutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.logout_dialog, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = "Logout Confirmation"

        val messageTextView = dialogView.findViewById<TextView>(R.id.dialog_message)
        messageTextView.text = "Are you sure you want to logout?"

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setPositiveButton("Logout") { dialog, which ->
            // Perform logout action here

            LogoutFunction()
            // For example, navigate to the login screen or sign out the user
            // Add your logout logic here
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


//    private fun WriteOntoDatabase(user: FirebaseUser) {
//        // Writing into the Realtime Database
//
//        // Step 2: Create a data class to represent the data you want to store
//        data class MyData(val attendanceID: String, val attendanceName: String)
//        // Step 3: Create an instance of the data class with the new key-value pair
//        val newData = MyData("EnterAttendanceID", "EnterAttendanceName")
//
//        // Step 4: Use the reference to the Firebase database to push the data to the database
//        // Push the new data to the database under a new unique key
//        val newDataRef =
//            database.child("whatever_is_the_parent_keyFolder_in_Firebase").child("SessionName")
//        newDataRef.setValue(newData)
//        Toast.makeText(this, "Running", Toast.LENGTH_SHORT).show()
//    }

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
                    for (i in 0 until courses.length()) {
                        val course = courses.getJSONObject(i)
                        if (course.getString("categoryid") != "0") {
                            val courseId = course.getString("id")
                            val courseName = course.getString("fullname")
                            Log.d("TAGXX", courseName)
                            Log.d("TAGXX", courseId)
                            FetchApplicantsList(courseId, courseName)
                        }


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
                    data.add(
                        objDashboard(
                            courseName,
                            applicant.toString(),
                            courseId,
                            courseNameToTokenMap[courseName].toString()
                        )
                    )
                    data.last().ClickedToken = courseNameToTokenMap[courseName].toString()
                    runOnUiThread {
                        binding.RecyclerView.adapter =
                            RecyclerViewDashboard_Adapter(data, currentTOKEN, courseNameToTokenMap)

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
//    val cn_UID = "8qd1AmvNspdMriLQmKGh1wvxZNm1"
//    val ps_UID = "7zXfIyDi76dmKlzkjsGHvsLCVMQ2"
//    val se_UID = "2S8Gy6tcwKQ8ABIqVJMA2ztthuR2"
    // Now we don't need to hard code this as we have also fetched the UID from firebase


    // Now compare the CurrentToken and the HardCoded Tokens
    // Here

    var currentToken: String? =
        null // Initialize currentToken to null // Token retrieved from Firebase after Authentication (Login)
//    var anotherData: String? = null

    private fun readDataOnce(callback: (MutableMap<String, String>) -> Unit) {
        // Add listener to database reference for a single value event
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value


                for (snapshot in dataSnapshot.children) {
                    // Iterate through each child node
                    val key = snapshot.key // Retrieve the key
                    val value = snapshot.value // Retrieve the value

                    // Check if the current key matches any of the desired keys
                    when (key) {
                        userUID -> {
                            currentToken = value.toString()
                            // Do something with the retrieved data
                            currentTOKEN = currentToken as String
                            Log.d("TokenHere", currentToken!!)
                            // You can break the loop here if you only want one token

                        }

                    }
                }


//
//                // Retrieve token and courseName for uid1 and uid2
//                val uid1Token = uid1Snapshot.child("token").value.toString()
//                val uid1CourseName = uid1Snapshot.child("courseName").value.toString()
//
//                val uid2Token = uid2Snapshot.child("token").value.toString()
//                val uid2CourseName = uid2Snapshot.child("courseName").value.toString()

                // Do something with the retrieved data
                currentTOKEN = currentToken as String

//                courseNameToTokenMap[]=uid1Token
                // Here you can use both currentToken and anotherData as needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Toast.makeText(applicationContext, "Failed to read value.", Toast.LENGTH_SHORT)
                    .show()
            }
        })


        infoReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (uidSnapshot in dataSnapshot.children) {
                    val currentKey = uidSnapshot.key // Get the key (UID of the teacher)

                    // Check if the current key matches the UID of the logged-in user

                    // Retrieve token and course name for the logged-in user
                    token = uidSnapshot.child("token").getValue(String::class.java) ?: ""
                    courseName = uidSnapshot.child("courseName").getValue(String::class.java) ?: ""
                    courseNameToTokenMap[courseName] = token

                    // Store the values for the logged-in user
                    // You can use these values as needed within this block
                    Log.d("UserValues", "Token: $token, CourseName: $courseName")


                }
                callback(courseNameToTokenMap)
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })


    }

    //---------------------------Search course starts here----------------------------------


    private fun showEditTextDialog() {
        // Inflate the custom dialog layout
        val customLayout = layoutInflater.inflate(R.layout.search_dialog_box, null)

        // Find the EditText view
        val editText = customLayout.findViewById<EditText>(R.id.edit_text_search)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Call filterCourses function when text changes
                filterCourses(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })

        // Create and show the dialog with the custom layout
        AlertDialog.Builder(this).setTitle("Search Course").setView(customLayout)
            .setPositiveButton("OK") { dialog, _ ->
                // Handle OK button click (if needed)
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun filterCourses(courseName: String) {

        flag = true

        val filteredList = mutableListOf<objDashboard>()

        // Iterate through all courses and set visibility based on course name
        for (course in data) {
            val isGone = course.name.lowercase().startsWith(courseName.lowercase())
            course.isGone = isGone
            if (isGone) {
                filteredList.add(course)
            }
        }

        // Update RecyclerView with filtered data
        (binding.RecyclerView.adapter as? RecyclerViewDashboard_Adapter)?.apply {
            data.clear()
            data.addAll(filteredList)
            notifyDataSetChanged()
        }


        binding.showAllcoursesButton.setOnClickListener {

            refreshAllCourses(filteredList)

        }
    }

    private fun refreshAllCourses(filteredList: MutableList<objDashboard>) {
        if (flag) {

            hideAllCourses()
            filteredList.clear()
            FetchUserName()


        }
    }

    private fun hideAllCourses() {

        flag = false
        // Iterate through data list and set isGone property to true for all items
        for (course in data) {
            course.isGone = true
        }

        // Clear the data list to remove all courses
        data.clear()

        // Notify the adapter to update the RecyclerView
        (binding.RecyclerView.adapter as? RecyclerViewDashboard_Adapter)?.apply {
            notifyDataSetChanged()
        }
    }

    //-----------------------------Search course ends here--------------------------------------------


    // ----------------------------------- Navigation Functions -----------------------------------------------

    private fun setupNavigationPane() {
        // Set up touch listener for non-navigation pane area to close the navigation pane
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isNavigationVisible) {
                val outRect = Rect()
                binding.navigationPane.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // User touched outside the navigation pane, close it
                    openNavigationPane()
                }
            }
            false
        }
    }

    private fun openNavigationPane() {
        // Show the navigation pane with animation
        binding.navigationPane.animate().translationX(0f).setDuration(500).start()
        binding.navigationPane.visibility = View.VISIBLE

        // Fade in animation for the overlay
        binding.overlay.alpha = 0f // Start with transparency
        binding.overlay.visibility = View.VISIBLE // Make overlay visible
        binding.overlay.animate().alpha(1f).setDuration(500).start() // Fade in with duration


        // Set isNavigationVisible to true
        isNavigationVisible = true
    }

    private fun closeNavigationPane() {
        // Hide the navigation pane with animation
        binding.navigationPane.animate().translationX(-binding.navigationPane.width.toFloat())
            .setDuration(500).withEndAction {
                binding.navigationPane.visibility = View.GONE
            }.start()

        // Fade out animation for the overlay
        binding.overlay.animate().alpha(0f).setDuration(500).withEndAction {
            binding.overlay.visibility = View.GONE // Hide the overlay when animation completes
        }.start()

        // Set isNavigationVisible to false
        isNavigationVisible = false
    }

// ----------------------------------- Navigation  Ended ---------------------------------------

}