package com.miniproject.attendx.attendance

import java.io.Serializable

data class markedDataObj(
    var statusID:String,
    var studentID:String,
    val takenByID:String,
    var sessionID:String,
    var studentName:String,
    var presentOrAbsent:String
):Serializable