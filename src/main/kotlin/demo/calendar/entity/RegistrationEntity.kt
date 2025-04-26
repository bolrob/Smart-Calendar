package demo.calendar.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "registrations")
data class RegistrationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    val event: EventEntity,

    @Column(nullable=false)
    val registrationTime: LocalDateTime,

    )