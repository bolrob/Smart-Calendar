package demo.calendar.entity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "events")
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
