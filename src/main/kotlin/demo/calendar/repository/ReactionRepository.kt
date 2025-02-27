package demo.calendar.repository

import jakarta.persistence.*

@Entity
@Table(name = "reactions")

class ReactionRepository (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    val raiting: Float,
)