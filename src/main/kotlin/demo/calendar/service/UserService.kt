package demo.calendar.service

import demo.calendar.dto.SingUpRequest
import demo.calendar.dto.User
import demo.calendar.entity.UserEntity
import demo.calendar.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun registerUser(request: SingUpRequest): String {
        val user = userRepository.findByTg(request.tg)
        if (user != null) throw RuntimeException("Пользователь уже зарегистрирован")
        userRepository.save(UserEntity(
            username = request.userName,
            email = request.email,
            phone = request.phone,
            tg = request.tg
        ))
        return "Пользователь успешно зарегистрирован"
    }
}