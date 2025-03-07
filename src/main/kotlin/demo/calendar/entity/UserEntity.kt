package demo.calendar.entity
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(unique = true)
    val email: String?,

    @Column(unique = true)
    val phone: String?,

    @Column(unique = true, nullable=false)
    val tg: String

)
