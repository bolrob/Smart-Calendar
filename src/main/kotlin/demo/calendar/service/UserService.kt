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
    fun createToken(user: UserEntity): String {
        val token = TokenEntity(
            token_value = UUID.randomUUID().toString(),
            user = user
        )
        tokenRepository.save(token)
        return token.token_value
    }
    fun tokenIsValid(token: TokenEntity?) {
        if (token == null) throw NotValidTokenException("No user exists with such token")
        if (token.revoked) throw NotValidTokenException("User's token has been already revoked")
    }
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
        if (user.username != request.userName) throw UserNotFoundException("User with such tg has different username")
        if (user.password != request.password) throw WrongPasswordException("User with such tg has different password")
        val token = createToken(user)
        return token
    }
    @Transactional
    fun manageUser(token: String, request: ManageRequest): UserResponse {
        val tEntity = tokenRepository.findByValue(token)
        tokenIsValid(tEntity)
        val user = tEntity!!.user
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
    fun logout(token: String) {
        val tEntity = tokenRepository.findByValue(token)
        tokenIsValid(tEntity)
        tokenRepository.save(TokenEntity(
            id = tEntity!!.id,
            token_value = tEntity.token_value,
            user = tEntity.user,
            revoked = true
        ))
    }
}