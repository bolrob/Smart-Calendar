package service

import demo.calendar.dto.SingUpRequest
import demo.calendar.dto.UserResponse
import demo.calendar.entity.UserEntity
import demo.calendar.repository.UserRepository
import demo.calendar.service.UserService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)
    @Test
    fun `Регистрация пользователя, когда пользователь правильно регистрируется`(){
        val newRequest = SingUpRequest(
            userName = "Ostin",
            phone = "891234567829",
            email = "PUPUPUpupunia@popatarakana",
            tg = "@SIGMABOY",)
        every { userRepository.findByTg(newRequest.tg) } returns null
        every {userRepository.save(UserEntity(
            username=newRequest.userName, phone=newRequest.phone, email=newRequest.email, tg=newRequest.tg))} answers{firstArg()}
        val response = userService.registerUser(newRequest)
        response shouldBe UserResponse(userName=newRequest.userName, phone=newRequest.phone, email=newRequest.email, tg=newRequest.tg)
    }


}
//тест проходит, если закомментить содержимое файлов UserController и CalendarController