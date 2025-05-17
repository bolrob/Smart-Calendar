package demo.calendar.dto

import demo.calendar.entity.CalendarEntity
import demo.calendar.entity.UserEntity

data class ValidRequestResponse(
    val user: UserEntity,
    val calendar: CalendarEntity,
    val accessType: String
)