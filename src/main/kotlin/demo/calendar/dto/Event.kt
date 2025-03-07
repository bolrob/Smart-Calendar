package demo.calendar.dto

import java.time.LocalDateTime

data class Event(
    val id: Long,
    val title: String,
    val description: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val organizerId: Long,
    val status: String = "active",
    val averageRating: Double = 0.0,
)
