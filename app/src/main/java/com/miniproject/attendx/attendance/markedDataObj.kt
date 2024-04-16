package com.miniproject.attendx.attendance

import java.io.Serializable

data class markedDataObj(
    val statusID:String,
    val studentID:String,
    val takenByID:String,
    val sessionID:String,
    val studentName:String,
    val presentOrAbsent:String
):Serializable
