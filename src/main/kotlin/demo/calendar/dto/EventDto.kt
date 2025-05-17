package demo.calendar.dto

import java.time.LocalDateTime

data class EventDto(
    val title: String,
    val description: String?,
    val address: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String = "ACTIVE"
) {
    fun toResponse(id: Long) = EventResponse(
        id = id,
        title = title,
        description = description,
        address = address,
        startTime = startTime,
        endTime = endTime,
        status = status
    )
}