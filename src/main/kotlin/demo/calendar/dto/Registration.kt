package demo.calendar.dto

data class Registration(
    val id: Long,
    val user: User,
    val calendar: Calendar
) {
}