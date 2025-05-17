package demo.calendar.dto

import java.time.LocalDateTime

data class EventResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val address: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String = "ACTIVE"
)