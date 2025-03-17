package demo.calendar.entity
import jakarta.persistence.*

//idx_user_phone ускоряет поиск пользователей по номеру телефона
//idx_user_tg  ускоряет поиск пользователей по тг
//по почте, имени пользователя не имеет смысла организовывать быстрый поиск, так как люди чаще всего ищут других людей
//по номеру телефона (используя свою телефонную книгу) или по тг(так как в последнее время эта соцсеть популярней, чем почта),
//имя пользователя тоже часто совпадает с тг, поэтому проще искать по нему
@Entity
@Table(name = "users", indexes=[Index(name = "idx_users_phone", columnList = "phone"),
    Index(name="idx_users_tg", columnList = "tg")])
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
