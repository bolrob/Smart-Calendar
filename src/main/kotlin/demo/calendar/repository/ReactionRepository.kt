package demo.calendar.repository

import demo.calendar.entity.EventEntity
import demo.calendar.entity.ReactionEntity
import demo.calendar.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ReactionRepository: JpaRepository<ReactionEntity, Long> {
    fun findByEventAndUser(event: EventEntity, user: UserEntity): ReactionEntity?
    fun findByEvent(event: EventEntity): List<ReactionEntity>
}