package demo.calendar.entity
import jakarta.persistence.*

@Entity
@Table(name = "calendars")
data class CalendarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val calendarName: String

)
