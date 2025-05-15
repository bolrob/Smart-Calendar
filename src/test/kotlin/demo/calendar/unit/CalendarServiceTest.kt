package demo.calendar.unit

import demo.calendar.dto.*
import demo.calendar.entity.CalendarEntity
import demo.calendar.entity.TokenEntity
import demo.calendar.entity.UserEntity
import demo.calendar.entity.UserToCalendarEntity
import demo.calendar.exception.*
import demo.calendar.repository.*
import demo.calendar.service.CalendarService
import demo.calendar.service.UserService
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.extensions.system.captureStandardErr
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class CalendarServiceTest {
    private val userToCalendarRepository = mockk<UserToCalendarRepository>()
    private val calendarRepository = mockk<CalendarRepository>()
    private val userRepository = mockk<UserRepository>()
    private val eventRepository = mockk<EventRepository>()
    private val tokenRepository = mockk<TokenRepository>()
    private val userService = UserService(userRepository, tokenRepository)
    private val calendarService = CalendarService(userToCalendarRepository, calendarRepository, eventRepository, userRepository, tokenRepository, userService)

    @Test
    fun `Создание календаря, календарь с таким тегом уже существует`() {
        val request = CreateCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description"
        )
        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = UserEntity(
                username = "username",
                email = null,
                phone = null,
                tg = "tg",
                password = "password"
            ),
            revoked = false
        )

        every {calendarRepository.findByTeg(request.teg)} returns CalendarEntity(
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.teg,
            active = true
        )
        shouldThrow<InvalidTegException> {
            calendarService.createCalendar("token", request)
        }
    }

    @Test
    fun `Создание календаря, успешное выполнение`() {
        val request = CreateCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description"
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns null
        every { calendarRepository.save(calendar) } returns calendar
        every { userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar=calendar, access_type = "ADMINISTRATOR")) } answers { firstArg() }
        val response = calendarService.createCalendar("token", request)
        response shouldBe CalendarResponse(
            calendarName=request.calendarName,
            public=request.public,
            teg=request.teg,
            active=true,
            description = request.description,
        )
    }

    @Test
    fun `Изменение календаря, календаря с данным тегом не существует`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns null
        shouldThrow<InvalidTegException> { calendarService.manageCalendar("token", request) }
    }

    @Test
    fun `Изменение календаря, календаря с данным тегом неактивен`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = false
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        shouldThrow<NotActiveCalendarException> { calendarService.manageCalendar("token", request) }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для взаимодействия с календарем`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = false,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrow<PrivateCalendarException> { calendarService.manageCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(user = user, calendar = calendar, access_type = "DELETED")
        shouldThrow<PrivateCalendarException> { calendarService.manageCalendar("token", request) }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для изменение данного публичного календаря`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendar, you can only view it.") { calendarService.manageCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(user = user, calendar = calendar, access_type = "DELETED")
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendar, you can only view it.") { calendarService.manageCalendar("token", request) }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для изменение данного календаря`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(0, user, calendar, "VIEWER")
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change this calendar.") { calendarService.manageCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(0, user, calendar, "ORGANIZER")
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change this calendar.") { calendarService.manageCalendar("token", request) }
    }

    @Test
    fun `Изменение календаря, успешное выполнение`() {
        val request = ManageCalendarRequest(
            calendarName = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        var calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            teg = request.teg,
            description = request.description,
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(0, user, calendar, "ADMINISTRATOR")
        calendar = CalendarEntity(
            id = 0,
            calendar_name = request.calendarName,
            public = request.public,
            active = request.active,
            description = request.description,
            teg = request.teg
        )
        every { calendarRepository.save(calendar) } answers { firstArg() }
        calendarService.manageCalendar("token", request) shouldBe calendar.toCalendar()
    }

    @Test
    fun `Изменение прав пользователя, календаря с данным тегом не существует`() {
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns null
        shouldThrow<InvalidTegException> { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, календарь с данным тегом неактивен`() {
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = false
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        shouldThrow<NotActiveCalendarException> { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя недостаточно прав для взаимодейтсвия с данным календарем`() {
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = false,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrow<PrivateCalendarException> { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "DELETED"
        )
        shouldThrow<PrivateCalendarException> { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя недостаточно прав для изменения данного публичного календаря`() {
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendars users, you can only view it.") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "DELETED"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendars users, you can only view it.") { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, пользователя с данным тг не существует`() {
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password",
            active = false
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg("teg") } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        every { userRepository.findByTg(request.userTg) } returns null
        shouldThrow<UserNotFoundException> { calendarService.manageUsers("token", request) }
        every { userRepository.findByTg(request.userTg) } returns user
        shouldThrow<UserNotFoundException> { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя не достаточно прав для изменения прав другого пользователя`() {
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password",
            active = true
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg("teg") } returns calendar
        var request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        val user2 = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg2",
            password = "password",
            active = true
        )
        every { userRepository.findByTg(request.userTg) } returns user2
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "VIEWER"
        )
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "VIEWER"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendars users") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ORGANIZER"
        )
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ORGANIZER"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change users access type if it's higher then yours") { calendarService.manageUsers("token", request) }
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "MODERATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change users access type if it's higher then yours") { calendarService.manageUsers("token", request) }
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change users access type if it's higher then yours") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "MODERATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change users access type if it's higher then yours") { calendarService.manageUsers("token", request) }
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "ADMINISTRATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change users access type if it's higher then yours") { calendarService.manageUsers("token", request) }
        request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg",
            accessType = "DELETED"
        )
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ORGANIZER"
        )
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ORGANIZER"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of user with higher access the you") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of user with higher access the you") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of user with higher access the you") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of user with higher access the you") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of user with higher access the you") { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to change access type of other administrator") { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Изменение прав пользователя, успешное выполнение`() {
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password",
            active = true
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )
        val user2 = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg2",
            password = "password",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg("teg") } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        val request = ManageUsersRequest(
            teg = "teg",
            userTg = "tg2",
            accessType = "VIEWER"
        )
        every { userRepository.findByTg(user2.tg) } returns user2
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns null
        every { userToCalendarRepository.save(UserToCalendarEntity(
            user = user2,
            calendar = calendar,
            access_type = request.accessType
        )) } answers { firstArg() }
        shouldNotThrow<LimitedAccessRightsException> { calendarService.manageUsers("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user2, calendar) } returns UserToCalendarEntity(
            id = 0,
            user = user2,
            calendar = calendar,
            access_type = "VIEWER"
        )
        every { userToCalendarRepository.save(UserToCalendarEntity(
            id = 0,
            user = user2,
            calendar = calendar,
            access_type = request.accessType
        )) } answers { firstArg() }
        shouldNotThrow<LimitedAccessRightsException> { calendarService.manageUsers("token", request) }
    }

    @Test
    fun `Удаление календаря, календаря с данным тегом не существует`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns null
        shouldThrow<InvalidTegException> { calendarService.deleteCalendar("token", request) }
    }

    @Test
    fun `Удаление календаря, календарь с данным тегом неактивен`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = false
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        shouldThrow<NotActiveCalendarException> { calendarService.deleteCalendar("token", request) }
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для взаимодейтсвия с данным календарем`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = false,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrow<PrivateCalendarException> { calendarService.deleteCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "DELETED"
        )
        shouldThrow<PrivateCalendarException> { calendarService.deleteCalendar("token", request) }
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для изменения данного публичного календаря`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns null
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendar, you can only view it.") { calendarService.deleteCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "DELETED"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to manage this calendar, you can only view it.") { calendarService.deleteCalendar("token", request) }
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для удаления данного календаря`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "VIEWER"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to delete this calendar.") { calendarService.deleteCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ORGANIZER"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to delete this calendar.") { calendarService.deleteCalendar("token", request) }
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "MODERATOR"
        )
        shouldThrowWithMessage<LimitedAccessRightsException>("You do not have access rights to delete this calendar.") { calendarService.deleteCalendar("token", request) }
    }
    @Test
    fun `Удаление календаря, успешное выполнение`() {
        val request = DeleteCalendarRequest(
            teg = "teg",
        )
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByTeg(request.teg) } returns calendar
        every { userToCalendarRepository.findByUserAndCalendar(user, calendar) } returns UserToCalendarEntity(
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )
        every { calendarRepository.save(CalendarEntity(
            id = calendar.id,
            calendar_name = calendar.calendar_name,
            description = calendar.description,
            public = calendar.public,
            teg = calendar.teg,
            active = false
        )) } answers { firstArg() }
        shouldNotThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("token", request) }
    }

    @Test
    fun `Получение календарей, введен неправильный тип`() {
        val size = 1
        val page = 0
        val sortBy = null
        val type = "TYPE"
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        shouldThrow<BadRequestException> { calendarService.getCalendars("token", page, size, sortBy, type) }
    }

    @Test
    fun `Получение календарей, успешное выполнение с типом PUBLIC`() {
        val size = 1
        val page = 0
        val sortBy = null
        val sort = Sort.by("id")
        val type = "PUBLIC"
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { calendarRepository.findByPublic(true, pageable = PageRequest.of(page, size, sort)) } returns PageImpl(
            listOf(calendar))
        calendarService.getCalendars("token", page, size, sortBy, type) shouldBe listOf(calendar.toCalendar())
    }

    @Test
    fun `Получение календарей, успешное выполнение с типом ALLOWED`() {
        val size = 1
        val page = 0
        val sortBy = null
        val sort = Sort.by("id")
        val type = "ALLOWED"
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { userToCalendarRepository.findByUser(user, PageRequest.of(page, size, sort)) } returns PageImpl(listOf(UserToCalendarEntity(
            id = 0,
            user = user,
            calendar = calendar,
            access_type = "VIEWER"
        )))
        calendarService.getCalendars("token", page, size, sortBy, type) shouldBe listOf(calendar.toCalendar())
    }

    @Test
    fun `Получение календарей, успешное выполнение с типом OWN`() {
        val size = 1
        val page = 0
        val sortBy = null
        val sort = Sort.by("id")
        val type = "OWN"
        val user = UserEntity(
            username = "username",
            email = null,
            phone = null,
            tg = "tg",
            password = "password"
        )
        val calendar = CalendarEntity(
            id = 0,
            calendar_name = "calendar",
            public = true,
            teg = "teg",
            description = "description",
            active = true
        )

        every { tokenRepository.findByToken("token") } returns TokenEntity(
            token = "token",
            user = user,
            revoked = false
        )
        every { userToCalendarRepository.findByUser(user, PageRequest.of(page, size, sort)) } returns PageImpl(listOf(UserToCalendarEntity(
            id = 0,
            user = user,
            calendar = calendar,
            access_type = "ADMINISTRATOR"
        )))
        calendarService.getCalendars("token", page, size, sortBy, type) shouldBe listOf(calendar.toCalendar())
    }
}