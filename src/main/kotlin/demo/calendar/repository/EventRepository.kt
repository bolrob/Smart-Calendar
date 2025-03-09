package demo.calendar.repository

import demo.calendar.entity.EventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EventRepository: JpaRepository<EventEntity, Long> {
    fun findByTitle(title: String): List<EventEntity>
}