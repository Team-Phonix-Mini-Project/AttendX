package com.miniproject.attendx.attendance

import java.io.Serializable

data class MarkingAttDataObj(
    val courseID: String,
    val attendanceID: String,
    val sessionID: String,
    val studentName: String,
    val studentID: String,
    val statusID: String,
    val Temp_array: ArrayList<String>,
):Serializable