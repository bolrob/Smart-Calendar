package demo.calendar.repository

import demo.calendar.dto.EventToCalendar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventToCalendarRepository: JpaRepository<EventToCalendar, Long> {
    override fun findById(id: Long): Optional<EventToCalendar>
}