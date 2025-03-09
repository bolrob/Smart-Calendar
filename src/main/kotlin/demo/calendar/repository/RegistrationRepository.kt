package demo.calendar.repository

import demo.calendar.entity.RegistrationEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RegistrationRepository: JpaRepository<RegistrationEntity, Long> {
}