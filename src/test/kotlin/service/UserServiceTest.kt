package service

import demo.calendar.dto.SingUpRequest
import demo.calendar.entity.UserEntity
import demo.calendar.exception.UserAlreadyRegisteredException
import demo.calendar.repository.UserRepository
import demo.calendar.service.UserService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows


class UserServiceTest {
    private val userRepository = mockk<UserRepository>()

    private val userService = UserService(userRepository)

    @Test
    fun `Регисграция пользователя, пользователь уже зарегестрирован`() {
        val newUser = SingUpRequest(
            userName = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
        )
        every { userRepository.findByTg(newUser.tg) } returns UserEntity(
            username = newUser.userName,
            phone = newUser.phone,
            email = newUser.email,
            tg = newUser.tg
        )

        val exception = assertThrows(UserAlreadyRegisteredException::class.java) {
            userService.registerUser(newUser)
        }

        exception.message shouldBe "User with this tg is already registered"

    }
}