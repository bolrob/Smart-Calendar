package demo.calendar.repository

import demo.calendar.entity.UserToCalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserToCalendarRepository: JpaRepository<UserToCalendarEntity, Long> {
}