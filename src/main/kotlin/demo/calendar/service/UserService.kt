package demo.calendar.service

import demo.calendar.dto.AuthorizeRequest
import demo.calendar.dto.ManageRequest
import demo.calendar.dto.SingUpRequest
import demo.calendar.dto.UserResponse
import demo.calendar.entity.UserEntity
import demo.calendar.exception.*
import demo.calendar.repository.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registerUser(request: SingUpRequest): UserResponse {
        logger.debug("Начало регистрации пользователя: {}", request.tg)
        val user = userRepository.findByTg(request.tg)
        if (user != null){
            throw UserAlreadyRegisteredException("User with this tg is already registered")
        }
        userRepository.save(
            UserEntity(
                username = request.userName,
                email = request.email,
                phone = request.phone,
                tg = request.tg,
                password = request.password
            )
        )
        logger.info("Пользователь с такими данными userName: {}, tg: {} успешно сохранен", request.userName, request.tg)
        return UserResponse(
            userName = request.userName,
            email = request.email,
            phone = request.phone,
            tg = request.tg,
            password = request.password
        )
    }

    fun authorizeUser(request: AuthorizeRequest): String {
        logger.debug("Начало авторизации пользователя: {}", request.tg)
        val user = userRepository.findByTg(request.tg) ?: throw UserNotFoundException("User with this tg not found")
        if (!user.active) {
            throw UserIsDeactivatedException("User with this tg is deactivated")
        }
        if (user.username != request.userName){
            throw WrongUserException("User with such tg has different username")
        }
        if (user.password != request.password){
            throw WrongPasswordException("User with such tg has different password")
        }
        val token = tokenService.createToken(user)
        logger.info("Авторизация прошла успешно, для пользователя с tg: {}", request.tg)
        return token
    }

    @Transactional
    fun manageUser(token: String, request: ManageRequest): UserResponse {
        val tEntity = tokenService.findByToken(token)
        tokenService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        logger.debug("Начало обновления пользователя с тг: {}", user.tg)
        if (request.oldPassword != user.password) {
            throw WrongPasswordException("User with such token has different password")
        }
        val newUser = UserEntity(
            id = user.id,
            username = request.userName ?: user.username,
            email = request.email ?: user.email,
            phone = request.phone ?: user.phone,
            tg = user.tg,
            password = request.password ?: user.password
        )
        userRepository.save(newUser)
        logger.info("Обновление пользователя с тг {} прошло успешно", user.tg)
        return UserResponse(
            userName = newUser.username,
            email = newUser.email,
            phone = newUser.phone,
            tg = user.tg,
            password = newUser.password
        )
    }
    fun logout(token: String) {
        val tEntity = tokenService.findByToken(token)
        tokenService.tokenIsValid(tEntity)
        logger.debug("Попытка выхода пользователя с тг {} из аккаунта", tEntity!!.user.tg)
        tokenService.revokeToken(tEntity)
        logger.info("Выход пользователя с тг {} прошел успешно", tEntity.user.tg)
    }
    @Transactional
    fun deleteUser(token: String, password: String) {
        val tEntity = tokenService.findByToken(token)
        tokenService.tokenIsValid(tEntity)
        logger.debug("Начало удаления пользователя с тг {}", tEntity!!.user.tg)
        val user = tEntity.user
        if (user.password != password) {
            throw WrongPasswordException("User with such token has different password")
        }
        tokenService.revokeToken(tEntity)
        userRepository.save(UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            phone = user.phone,
            tg = user.tg,
            password = user.password,
            active = false
        ))
        logger.info("Удаление пользователя с тг {} прошло успешно", user.tg)
    }
}