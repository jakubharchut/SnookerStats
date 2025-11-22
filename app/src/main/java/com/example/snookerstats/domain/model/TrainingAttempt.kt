package com.example.snookerstats.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class TrainingAttempt(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val trainingType: String = "LINE_UP",
    @ServerTimestamp
    val date: Date? = null,
    val score: Int = 0,
    val durationInSeconds: Long = 0,
    val pottedBalls: List<String> = emptyList(), // Storing ball names as strings
    val missedBalls: List<String> = emptyList()
)
