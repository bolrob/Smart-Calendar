package demo.calendar.repository

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserRepository(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val tg: String,

    @Column(nullable = false)
    val login: String,

    @Column(nullable = true)
    val phone: String?,

    @Column(nullable = true)
    val email: String? = null,

    @Column(nullable = true)
    val token: String? = null,
)