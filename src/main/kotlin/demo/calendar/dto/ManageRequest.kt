package demo.calendar.dto

data class ManageRequest(
    val userName: String,
    val phone: String,
    val email: String,
    val tg: String,
    val password: String,
    val oldPassword: String
) {
}