package demo.calendar.repository

import demo.calendar.entity.EventToCalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventToCalendarRepository: JpaRepository<EventToCalendarEntity, Long> {
}