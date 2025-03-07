package demo.calendar.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_to_calendar")
data class UserToCalendarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    val user: UserEntity,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    val calendar: CalendarEntity,
)