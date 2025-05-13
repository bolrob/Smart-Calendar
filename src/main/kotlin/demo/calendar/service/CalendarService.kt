package demo.calendar.service

import demo.calendar.dto.*
import demo.calendar.entity.CalendarEntity
import demo.calendar.entity.UserToCalendarEntity
import demo.calendar.exception.*
import demo.calendar.repository.*
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class CalendarService(
    private val userToCalendarRepository: UserToCalendarRepository,
    private val calendarRepository: CalendarRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val tokenRepository: TokenRepository,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun createCalendar(token: String, request:CreateCalendarRequest): CalendarResponse {
        val tEntity = tokenRepository.findByToken(token)
        userService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        logger.debug("Начало создания календаря пользователем с тг {}", user.tg)
        if(calendarRepository.findByTeg(request.teg) != null){
            logger.warn("Ошибка создания календаря с тегом {}, такой календарь уже существует", request.teg)
            throw InvalidTegException("Calendar with such teg already exists")
        }
        val calendar = calendarRepository.save(CalendarEntity(
            calendar_name = request.calendarName,
            is_public = request.isPublic,
            description = request.description,
            teg = request.teg,
            active = true
        ))
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar=calendar, access_type = "ADMINISTRATOR"))
        logger.info("Успешное создание календаря с такими данными calendarName: {}, teg: {} пользователем с тг {}", request.calendarName, request.teg, user.tg)
        return CalendarResponse(
            calendarName=request.calendarName,
            isPublic=request.isPublic,
            teg=request.teg,
            active=true,
            description = request.description,
        )
    }

    @Transactional
    fun manageCalendar(token: String, request: ManageCalendarRequest): CalendarResponse {
        val tEntity = tokenRepository.findByToken(token)
        userService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        logger.debug("Начало обновления календаря с тегом {} пользователем с тг: {}", request.teg, user.tg)
        val calendar = calendarRepository.findByTeg(request.teg)
        if(calendar == null) {
            logger.warn("Ошибка обновления календаря с тегом {}, такого календаря не существует", request.teg)
            throw InvalidTegException("Calendar with such teg does not exist")
        }
        if (!calendar.active){
            logger.warn("Ошибка обновления календаря с тегом {}, этот календарь удален", request.teg)
            throw NotActiveCalendarException("This calendar is not active")
        }
        val accessType = userToCalendarRepository.findByUserAndCalendar(user, calendar)?.access_type
        if (accessType == null || accessType == "DELETED"){
            if(!calendar.is_public){
                logger.warn("Ошибка обновления календаря с тегом {}, данный календарь приватный, у пользователя с тг {} нет к нему доступа", request.teg, user.tg)
                throw PrivateCalendarException("This calendar is private, you can't interact with it.")
            }
            logger.warn("Ошибка обновления календаря с тегом {}, права доступа пользователя с тг {} недостаточны для обновления календаря", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to manage this calendar, you can only view it.")
        }
        if (accessType == "VIEWER" || accessType == "ORGANIZER"){
            logger.warn("Ошибка обновления календаря с тегом {}, права доступа пользователя с тг {} недостаточны для обновления календаря", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to change this calendar.")
        }
        val newCalendar = CalendarEntity(
            id = calendar.id,
            calendar_name = request.calendarName,
            is_public = request.isPublic,
            active = request.active,
            teg = request.teg,
            description = request.description)
        calendarRepository.save(newCalendar)
        logger.info("Обновление календаря с тегом {} пользователем с тг {} прошло успешно", request.teg, user.tg)
        return CalendarResponse(calendarName = request.calendarName,
            isPublic = request.isPublic,
            active = request.active,
            teg = request.teg,
            description = request.description)
    }

    @Transactional
    fun manageUsers(token: String, request: ManageUsersRequest){
        val tEntity = tokenRepository.findByToken(token)
        userService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        logger.debug("Начало обновления прав доступа для пользователя с тг {} к календарю с тегом {}. Действие осуществляется пользователем с тг {}}", request.userTg, request.teg, user.tg)
        val calendar = calendarRepository.findByTeg(request.teg)
        if(calendar == null){
            logger.warn("Ошибка взаимодействия с календарем с тегом {}, такого календаря не существует", request.teg)
            throw InvalidTegException("Calendar with such teg does not exist")
        }
        if (!calendar.active){
            logger.warn("Ошибка обновления календаря с тегом {}, этот календарь удален", request.teg)
            throw NotActiveCalendarException("This calendar is not active")
        }
        val accessType = userToCalendarRepository.findByUserAndCalendar(user, calendar)?.access_type
        if (accessType == null || accessType == "DELETED"){
            if(!calendar.is_public){
                logger.warn("Ошибка взаимодествия с календарем с тегом {}, данный календарь приватный, у пользователя с тг {} нет к нему доступа", request.teg, user.tg)
                throw PrivateCalendarException("This calendar is private, you can't interact with it.")
            }
            logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} недостаточны для обновления календаря", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to manage this calendars users, you can only view it.")
        }
        val newUser = userRepository.findByTg(request.userTg)
        if(newUser == null) {
            logger.warn("Ошибка обновления прав доступа для пользователя с тг {}", request.userTg)
            throw UserNotFoundException("User with this tg not found")
        }
        val oldAccessType = userToCalendarRepository.findByUserAndCalendar(newUser, calendar)
        if (accessType == "VIEWER") {
            logger.warn("Ошибка взаимодествия с календарем {}, пользователь с тг {} может только его просматривать", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to manage this calendars users")
        }
        else if (accessType == "ORGANIZER" && request.accessType != "VIEWER" && request.accessType != "DELETED"){
            logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} ниже, чем права доступа которые он хочет назначить пользователю с тг {}", request.teg, user.tg, request.userTg)
            throw LimitedAccessRightsException("You do not have access rights to change users access type if it's higher then yours")
        }
        else if (accessType == "MODERATOR" && (request.accessType == "MODERATOR" || request.accessType == "ADMINISTRATOR")){
            logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} ниже, чем права доступа которые он хочет назначить пользователю с тг {}", request.teg, user.tg, request.userTg)
            throw LimitedAccessRightsException("You do not have access rights to change users access type if it's higher then yours")
        }
        if (oldAccessType != null) {
            if (accessType == "ORGANIZER" && oldAccessType.access_type != "VIEWER" && oldAccessType.access_type != "DELETED"){
                logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} ниже, чем права доступа пользователя с тг {}, которого он хочет обновить", request.teg, user.tg, request.userTg)
                throw LimitedAccessRightsException("You do not have access rights to change access type of user with higher access the you")
            }
            else if (accessType == "MODERATOR" && (oldAccessType.access_type == "MODERATOR" || oldAccessType.access_type == "ADMINISTRATOR")){
                logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} ниже, чем права доступа пользователя с тг {}, которого он хочет обновить", request.teg, user.tg, request.userTg)
                throw LimitedAccessRightsException("You do not have access rights to change access type of user with higher access the you")
            }
            else if (accessType == "ADMINISTRATOR" && oldAccessType.access_type == accessType){
                logger.warn("Ошибка взаимодествия с календарем {}, права доступа пользователя с тг {} ниже, чем права доступа пользователя с тг {}, которого он хочет обновить", request.teg, user.tg, request.userTg)
                throw LimitedAccessRightsException("You do not have access rights to change access type of other administrator")
            }
            userToCalendarRepository.save(UserToCalendarEntity(oldAccessType.id, newUser, calendar, request.accessType))
        }
        else {
            userToCalendarRepository.save(UserToCalendarEntity(user = newUser, calendar = calendar, access_type = request.accessType))
        }
        logger.info("Обновление прав доступа пользователем с тг {} для пользователя с тг {} к календарю с тегом {} прошло успешно.", user.tg, request.userTg, request.teg)
    }

    @Transactional
    fun deleteCalendar(token:String, request: DeleteCalendarRequest){
        val tEntity = tokenRepository.findByToken(token)
        userService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        logger.debug("Начало удаления календаря с тегом {} польсователем с тг {}", request.teg, user.tg)
        val calendar = calendarRepository.findByTeg(request.teg)
        if(calendar == null){
            logger.warn("Ошибка удаления календаря с тегом {}, такого календаря не существует", request.teg)
            throw InvalidTegException("Calendar with such teg does not exist")
        }
        if (!calendar.active){
            logger.warn("Ошибка удаления календаря с тегом {}, этот календарь уже удален", request.teg)
            throw NotActiveCalendarException("This calendar is not active")
        }
        val accessType = userToCalendarRepository.findByUserAndCalendar(user, calendar)?.access_type
        if (accessType == null || accessType == "DELETED"){
            if(!calendar.is_public){
                logger.warn("Ошибка удаления календаря с тегом {}, данный календарь приватный, у пользователя с тг {} нет к нему доступа", request.teg, user.tg)
                throw PrivateCalendarException("This calendar is private, you can't interact with it.")
            }
            logger.warn("Ошибка удаления календаря с тегом {}, права доступа пользователя с тг {} недостаточны для обновления календаря", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to manage this calendar, you can only view it.")
        }
        if (accessType == "VIEWER" || accessType == "ORGANIZER" || accessType == "MODERATOR"){
            logger.warn("Ошибка обновления календаря с тегом {}, права доступа пользователя с тг {} недостаточны для обновления календаря", request.teg, user.tg)
            throw LimitedAccessRightsException("You do not have access rights to delete this calendar.")
        }
        val newCalendar = CalendarEntity(
            id=calendar.id,
            calendar_name = calendar.calendar_name,
            is_public = calendar.is_public,
            active=false,
            teg = calendar.teg,
            description = calendar.description,
            )
        calendarRepository.save(newCalendar)
        logger.info("Удаление календаря с тегом {} пользователем с тг {} прошло успешно", request.teg, user.tg)
    }

    fun getCalendars(token: String, request: GetCalendarsRequest): List<CalendarResponse> {
        val tEntity = tokenRepository.findByToken(token)
        userService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        val sort = Sort.by(request.sortBy ?: "calendarName")
        val pageable = PageRequest.of(request.page, request.size, sort)
        when (request.type) {
            "PUBLIC" -> return calendarRepository.findByIsPublic(true, pageable).content.map {it.toCalendar()}
            "ALLOWED" -> {
                val tmp = userToCalendarRepository.findByUser(user, pageable).content.filter { it.access_type != "DELETED" }
                return tmp.map {it.calendar.toCalendar()}
            }
            "OWN" -> {
                val tmp = userToCalendarRepository.findByUser(user, pageable).content.filter { it.access_type == "ADMINISTRATOR" }
                return tmp.map {it.calendar.toCalendar()}
            }
            else -> throw BadRequestException("Bad request: unexpected type")
        }
    }
}