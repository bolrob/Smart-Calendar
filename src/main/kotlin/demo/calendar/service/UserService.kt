package demo.calendar.service

import demo.calendar.dto.AuthorizeRequest
import demo.calendar.dto.ManageRequest
import demo.calendar.dto.SingUpRequest
import demo.calendar.dto.UserResponse
import demo.calendar.entity.TokenEntity
import demo.calendar.entity.UserEntity
import demo.calendar.exception.NotValidTokenException
import demo.calendar.exception.UserAlreadyRegisteredException
import demo.calendar.exception.UserNotFoundException
import demo.calendar.exception.WrongPasswordException
import demo.calendar.repository.TokenRepository
import demo.calendar.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserService(
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository
) {
    fun createToken(user: UserEntity) =
        TokenEntity(
            token_value = UUID.randomUUID().toString(),
            user = user
        )

    @Transactional
    fun registerUser(request: SingUpRequest): UserResponse {
        val user = userRepository.findByTg(request.tg)
        if (user != null) throw UserAlreadyRegisteredException("User with this tg is already registered")
        userRepository.save(UserEntity(
            username = request.userName,
            email = request.email,
            phone = request.phone,
            tg = request.tg,
            password = request.password
        ))
        return UserResponse(userName = request.userName, email = request.email, phone = request.phone, tg = request.tg, password = request.password)
    }
    fun authorizeUser(request: AuthorizeRequest): String{
        val user = userRepository.findByTg(request.tg)
        if (user == null) throw UserNotFoundException("User with this tg not found")
        if (user.username != request.userName) throw WrongPasswordException("User with such username has different password")
        val token = createToken(user)
        return token.token_value
    }
    fun manageUser(token: String, request: ManageRequest): UserResponse {
        val user = tokenRepository.findByValue(token)?.user
        if (user == null) throw NotValidTokenException("No user exists with such token")
        if (request.oldPassword != user.password) throw WrongPasswordException("User with such token has different password")
        userRepository.save(UserEntity(
            id = user.id,
            username = request.userName,
            email = request.email,
            phone = request.phone,
            tg = request.tg,
            password = request.password
        ))
        return UserResponse(
            userName = request.userName,
            email = request.email,
            phone = request.phone,
            tg = request.tg,
            password = request.password
        )
    }
}