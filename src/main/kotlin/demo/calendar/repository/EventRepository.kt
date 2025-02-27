package demo.calendar.repository

import jakarta.persistence.*
import java.sql.Time

@Entity
@Table(name = "events")

class EventRepository (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val startTime: Time,

    @Column(nullable = false)
    val endTime: Time,

    @Column(nullable = false)
    val organizatorId: Long,
    //незнаю как связать это с таблицой ReactionRepository

)