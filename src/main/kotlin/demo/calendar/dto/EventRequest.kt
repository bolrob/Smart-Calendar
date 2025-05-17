package demo.calendar.dto

data class EventRequest(
    val teg: String,
    val id: Long,
    val event: EventDto
)
