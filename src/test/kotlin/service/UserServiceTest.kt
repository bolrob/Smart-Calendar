package service

import com.ninjasquad.springmockk.MockkBean
import demo.calendar.controller.UserController
import demo.calendar.dto.SingUpRequest
import demo.calendar.dto.UserResponse
import demo.calendar.entity.UserEntity
import demo.calendar.repository.UserRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {
    @MockkBean
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userController: UserController

    @Test
    fun `Регистрация пользователя, когда пользователь правильно регистрируется`(){
        val newRequest = SingUpRequest(
            userName = "Ostin",
            phone = "891234567829",
            email = "PUPUPUpupunia@popatarakana",
            tg = "@SIGMABOY",)
        every { userRepository.findByTg(newRequest.tg) } returns null
        every {userRepository.save(
            UserEntity(
                username=newRequest.userName, phone=newRequest.phone, email=newRequest.email, tg=newRequest.tg)
        )} answers{firstArg()}
        val response = userController.registerUser(newRequest)
        response shouldBe UserResponse(userName=newRequest.userName, phone=newRequest.phone, email=newRequest.email, tg=newRequest.tg)
    }
}