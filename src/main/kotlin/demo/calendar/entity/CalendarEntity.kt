package demo.calendar.entity
import jakarta.persistence.*

//в календаре у нас буквально 1 поле по которому мы можем искать, поэтому реализуем быстрый поиск по нему
@Entity
@Table(name = "calendars", indexes = [Index(name = "idx_calendar_name", columnList = "calendar_name")])
data class CalendarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val calendarName: String

)
