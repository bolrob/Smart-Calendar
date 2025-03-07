package demo.calendar.repository

import demo.calendar.dto.Event
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventRepository: JpaRepository<Event, Long> {
    override fun findById(id: Long): Optional<Event>

    fun findByTitle(title: String): List<Event>?
}