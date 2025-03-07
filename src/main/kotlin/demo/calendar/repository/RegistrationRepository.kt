package demo.calendar.repository

import demo.calendar.dto.Registration
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RegistrationRepository: JpaRepository<Registration, Long> {
    override fun findById(id: Long): Optional<Registration>
}