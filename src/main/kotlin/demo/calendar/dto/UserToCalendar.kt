package demo.calendar.dto

data class UserToCalendar(
    val id: Long,
    val calendar: Calendar,
    val user: User,
    val accessType: AccessType
) {
    enum class AccessType {
        VIEWER,
        MODERATOR,
        ADMIN
    }
}