package demo.calendar.controlleradvice

import demo.calendar.dto.ErrorResponse
import demo.calendar.exception.*
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Hidden
@RestControllerAdvice
class GeneralControllerAdvice {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(UserAlreadyRegisteredException::class)
    fun userAlreadyRegisteredExceptionHandler(exception: UserAlreadyRegisteredException) : ResponseEntity<ErrorResponse> {
        logger.warn("User with this tg is already registered", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "ALREADY REGISTERED"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun userNotFoundException(exception: UserNotFoundException) : ResponseEntity<ErrorResponse> {
        logger.warn("User with this tg not found", exception)
        return ResponseEntity.status(404).body(ErrorResponse(exception.message ?: "USER NOT FOUND"))
    }

    @ExceptionHandler(WrongUserException::class)
    fun wrongUserException(exception: WrongUserException) : ResponseEntity<ErrorResponse> {
        logger.warn("User with such tg has different username", exception)
        return ResponseEntity.status(401).body(ErrorResponse(exception.message ?: "WRONG USER"))
    }

    @ExceptionHandler(WrongPasswordException::class)
    fun wrongPasswordException(exception: WrongPasswordException) : ResponseEntity<ErrorResponse> {
        logger.warn("User with such token has different password", exception)
        return ResponseEntity.status(401).body(ErrorResponse(exception.message ?: "WRONG PASSWORD"))
    }

    @ExceptionHandler(NotValidTokenException::class)
    fun notValidTokenException(exception: NotValidTokenException) : ResponseEntity<ErrorResponse> {
        logger.warn("Not valid token", exception)
        return ResponseEntity.status(401).body(ErrorResponse(exception.message ?: "NOT VALID TOKEN"))
    }

    @ExceptionHandler(InvalidTegException::class)
    fun invalidTegException(exception: InvalidTegException) : ResponseEntity<ErrorResponse> {
        logger.warn("Not valid teg", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "NOT VALID TEG"))
    }

    @ExceptionHandler(LimitedAccessRightsException::class)
    fun limitedAccessRightsException(exception: LimitedAccessRightsException) : ResponseEntity<ErrorResponse> {
        logger.warn("The user does not have sufficient access rights to perform the desired action with the calendar", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "LIMITED ACCESS RIGHTS"))
    }

    @ExceptionHandler(NotActiveCalendarException::class)
    fun notActiveCalendarException(exception: NotActiveCalendarException) : ResponseEntity<ErrorResponse> {
        logger.warn("The calendar is not active", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "NOT ACTIVE CALENDAR"))
    }

    @ExceptionHandler(PrivateCalendarException::class)
    fun privateCalendarException(exception: PrivateCalendarException) : ResponseEntity<ErrorResponse> {
        logger.warn("The calendar is private, this user does not have access to it.", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "PRIVATE CALENDAR"))
    }

    @ExceptionHandler(UserIsDeactivatedException::class)
    fun userIsDeactivatedException(exception: UserIsDeactivatedException) : ResponseEntity<ErrorResponse> {
        logger.warn("User with this tg is deactivated", exception)
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "DEACTIVATED USER"))
    }

    @ExceptionHandler(BadRequestException::class)
    fun badRequestException(exception: BadRequestException) : ResponseEntity<ErrorResponse>{
        logger.warn("The bad request to get calendars that this user can interact with")
        return ResponseEntity.status(400).body(ErrorResponse(exception.message ?: "BAD REQUEST TO GET CALENDARS"))
    }
}