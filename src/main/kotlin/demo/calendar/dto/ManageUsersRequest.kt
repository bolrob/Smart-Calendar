package demo.calendar.dto

data class ManageUsersRequest(
    val teg: String,
    val userTg: String,
    val accessType: String
) {
    enum class AccessType {
        PUBLIC,
        ALLOWED,
        OWN
    }
}