package demo.calendar.dto

data class SingUpRequest(
    val userName: String,
    val phone: String?,
    val email: String?,
    val tg: String
) {
}