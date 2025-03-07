package demo.calendar.entity

import jakarta.persistence.*

@Entity
@Table(name = "event_to_calendar")
data class EventToCalendarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    val event: EventEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    val calendar: CalendarEntity,
    )
