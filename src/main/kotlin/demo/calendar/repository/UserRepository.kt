package demo.calendar.repository

import demo.calendar.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository: JpaRepository<UserEntity, Long> {
    fun findByPhone(phone: String): UserEntity?

    fun findByTg(tg: String): UserEntity?
}