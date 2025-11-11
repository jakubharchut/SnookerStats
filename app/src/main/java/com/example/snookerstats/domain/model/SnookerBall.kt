package com.example.snookerstats.domain.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.PropertyName

sealed class SnookerBall(val points: Int, val color: Color, val displayName: String) {
    // Firestore needs a way to identify the object type, a simple name field is best
    @get:PropertyName("name") val name: String get() = this::class.java.simpleName

    object Red : SnookerBall(1, Color.Red, "Czerwona")
    object Yellow : SnookerBall(2, Color(0xFFFDD835), "Żółta")
    object Green : SnookerBall(3, Color(0xFF2E7D32), "Zielona")
    object Brown : SnookerBall(4, Color(0xFF5D4037), "Brązowa")
    object Blue : SnookerBall(5, Color(0xFF1565C0), "Niebieska")
    object Pink : SnookerBall(6, Color(0xFFD81B60), "Różowa")
    object Black : SnookerBall(7, Color.Black, "Czarna")

    val contentColor: Color
        get() = when (this) {
            Yellow -> Color.Black
            else -> Color.White
        }

    // Default no-arg constructor for Firestore
    constructor() : this(0, Color.Transparent, "")
}
