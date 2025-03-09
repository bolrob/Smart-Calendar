package demo.calendar.repository

import demo.calendar.entity.CalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CalendarRepository: JpaRepository<CalendarEntity, Long> {
    fun findByName(name: String): CalendarEntity?
}