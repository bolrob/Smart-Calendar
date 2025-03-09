package demo.calendar.entity
import jakarta.persistence.*
import java.time.LocalDateTime

//idx_event_title реализует быстрый поиск по названию событий (на данный момент именно этот критерий мы считаем самым важным для пользователя),
//по описанию и адресу точно не имеет смысла организовывать поиск, по времени тоже, так как людям интересно скорее само событие, то есть что на нем происходит
//и потом по интересующему названию события, он выбирает уже удобно ли ему место проведения и время (тут мы подразумеваем что в названии события отражено то, что будет на нем происходить)
@Entity
@Table(name = "events", indexes=[Index(name = "idx_event_title", columnList = "title")])
data class EventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column
    val description: String?,

    @Column
    val address: String?,

    @Column
    val latitude: Double?,

    @Column
    val longitude: Double?,

    @Column(nullable=false)
    val startTime: LocalDateTime,

    @Column(nullable=false)
    val endTime: LocalDateTime,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", nullable = false)
    val user: UserEntity,

    @Column(nullable=false)
    val status: String="active",

    @Column
    val averageRating: Double=0.0
)
