package demo.calendar.repository

import demo.calendar.dto.Calendar
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CalendarRepository: JpaRepository<Calendar, Long> {
    override fun findById(id: Long): Optional<Calendar>

    fun findByName(name: String): Calendar?
}