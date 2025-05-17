package demo.calendar.integration

import demo.calendar.dto.*
import demo.calendar.entity.CalendarEntity
import demo.calendar.entity.TokenEntity
import demo.calendar.entity.UserEntity
import demo.calendar.entity.UserToCalendarEntity
import demo.calendar.exception.*
import demo.calendar.repository.CalendarRepository
import demo.calendar.repository.TokenRepository
import demo.calendar.repository.UserRepository
import demo.calendar.repository.UserToCalendarRepository
import demo.calendar.service.CalendarService
import demo.calendar.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@ActiveProfiles("test")
@SpringBootTest
class CalendarServiceTest {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var tokenRepository: TokenRepository

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var calendarService: CalendarService

    @Autowired
    lateinit var calendarRepository: CalendarRepository

    @Autowired
    lateinit var userToCalendarRepository: UserToCalendarRepository


    @AfterEach
    fun cleanup() {
        tokenRepository.deleteAll()
        userToCalendarRepository.deleteAll()
        calendarRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `Создание календаря, календарь с таким тегом уже существует`(){
        val newRequest = CreateCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED"
        )
        calendarRepository.save(CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description
        ))
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        shouldThrow<InvalidTegException> {
            calendarService.createCalendar("abc", newRequest)
        }
    }

    @Test
    fun `Создание календаря, успешное выполнение`(){
        val newRequest = CreateCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED"
        )
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val response = calendarService.createCalendar("abc", newRequest)
        response shouldBe CalendarResponse(
            calendarName = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            active =true,
            description = newRequest.description,)
    }

    @Test
    fun `Изменение календаря, календаря с данным тегом не существует`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        calendarRepository.save(CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = "AAAAAAAAA",
            description = newRequest.description,
            active=false
        ))
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        shouldThrow<InvalidTegException> {
            calendarService.manageCalendar("abc", newRequest)
        }
    }

    @Test
    fun `Изменение календаря, календаря с данным тегом неактивен`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        calendarRepository.save(CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description,
            active=false
        ))
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        shouldThrow<NotActiveCalendarException> {
            calendarService.manageCalendar("abc", newRequest)
        }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для взаимодействия с календарем`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = false,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description,
            active=newRequest.active)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        shouldThrow<PrivateCalendarException> {
            calendarService.manageCalendar("abc", newRequest)
        }
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "DELETED"
        ))
        shouldThrow<PrivateCalendarException> {
            calendarService.manageCalendar("abc", newRequest)
        }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для изменение данного публичного календаря`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description,
            active=newRequest.active)
        calendarRepository.save(newCalendar)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageCalendar("abc", newRequest)
        }
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "DELETED"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageCalendar("abc", newRequest)
        }
    }

    @Test
    fun `Изменение календаря, у пользователя недостаточно прав для изменение данного календаря`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description,
            active=newRequest.active)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "VIEWER"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageCalendar("abc", newRequest)
        }
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        val newToken = TokenEntity(
            token = "abcd",
            user = user)
        tokenRepository.save(newToken)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= newUser,
            calendar = newCalendar,
            access_type = "ORGANIZER"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageCalendar("abcd", newRequest)
        }
    }

    @Test
    fun `Изменение календаря, успешное выполнение`(){
        val newRequest = ManageCalendarRequest(
            calendarName = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            description = newRequest.description,
            active=newRequest.active)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "MODERATOR"
        ))
        calendarService.manageCalendar("abc", newRequest) shouldBe CalendarResponse(
            calendarName = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            active = newRequest.active,
            description = newRequest.description,)
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        val newToken = TokenEntity(
            token = "abcd",
            user = user)
        tokenRepository.save(newToken)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= newUser,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        calendarService.manageCalendar("abcd", newRequest) shouldBe CalendarResponse(
            calendarName = newRequest.calendarName,
            public = newRequest.public,
            teg = newRequest.teg,
            active = newRequest.active,
            description = newRequest.description,)
    }

    @Test
    fun `Изменение прав пользователя, календаря с данным тегом не существует`(){
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = true,
            teg = "rests",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        shouldThrow<InvalidTegException> {
            calendarService.manageUsers("abc", newRequest)
        }
    }

    @Test
    fun `Изменение прав пользователя, календарь с данным тегом неактивен`(){
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=false)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        shouldThrow<NotActiveCalendarException> {
            calendarService.manageUsers("abc", newRequest)
        }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя недостаточно прав для взаимодейтсвия с данным календарем`(){
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = false,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        shouldThrow<PrivateCalendarException> {
            calendarService.manageUsers("abc", newRequest)
        }
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "DELETED"
        ))
        shouldThrow<PrivateCalendarException> {
            calendarService.manageUsers("abc", newRequest)
        }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя недостаточно прав для изменения данного публичного календаря`(){
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newUser = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(newUser)
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc", newRequest)
        }
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "DELETED"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc", newRequest)
        }
    }

    @Test
    fun `Изменение прав пользователя, пользователя с данным тг не существует`(){
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user,)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        shouldThrow<UserNotFoundException> {
            calendarService.manageUsers("abc", newRequest)
        }
    }

    @Test
    fun `Изменение прав пользователя, у пользователя не достаточно прав для изменения прав другого пользователя`(){
        val newRequest0 = ManageUsersRequest(
            teg = "rest0",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newRequest01 = ManageUsersRequest(
            teg = "rest01",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user,)
        tokenRepository.save(token)
        shouldThrow<InvalidTegException> {
            calendarService.manageUsers("abc", newRequest0)
        }
        val newCalendar0 = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU0",
            public = true,
            teg = "rest0",
            description = "I AM TIRED0",
            active=false)
        calendarRepository.save(newCalendar0)
        shouldThrow<NotActiveCalendarException> {
            calendarService.manageUsers("abc", newRequest0)
        }
        val newCalendar01 = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU01",
            public = false,
            teg = "rest01",
            description = "I AM TIRED01",
            active=true)
        calendarRepository.save(newCalendar01)
        shouldThrow<PrivateCalendarException> {
            calendarService.manageUsers("abc", newRequest01)
        }
        val newCalendar02 = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU02",
            public = true,
            teg = "rest02",
            description = "I AM TIRED02",
            active=true)
        calendarRepository.save(newCalendar02)
        val newRequest02 = ManageUsersRequest(
            teg = "rest02",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        shouldThrow<PrivateCalendarException> {
            calendarService.manageUsers("abc", newRequest02)
        }
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar02, access_type = "DELETED"))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc", newRequest02)
        }
        val newCalendar = CalendarEntity(
            id = 0,
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "VIEWER"
        ))
        val user1 = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(user1)
        val token1 = TokenEntity(token="abcd", user=user1)
        tokenRepository.save(token1)
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc", newRequest)
        }
        val newRequest1 = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "MODERATOR"
        )
        val user2 = UserEntity(
            username = "Адольф1",
            phone = "14881",
            email = "AustrianPainter@nazi.de1",
            tg = "@amTheRealHitler13371",
            password = "BombardiroCrocodillo1",)
        userRepository.save(user2)
        val token2 = TokenEntity(
            token = "abc1",
            user = user2)
        tokenRepository.save(token2)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user2,
            calendar = newCalendar,
            access_type = "ORGANIZER"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc1", newRequest1)
        }
        val newRequest3 = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "MODERATOR"
        )
        val user3 = UserEntity(
            username = "Адольф3",
            phone = "14883",
            email = "AustrianPainter@nazi.de3",
            tg = "@amTheRealHitler13373",
            password = "BombardiroCrocodillo3",)
        userRepository.save(user3)
        val token3 = TokenEntity(
            token = "abc3",
            user = user3)
        tokenRepository.save(token3)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user3,
            calendar = newCalendar,
            access_type = "MODERATOR"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc3", newRequest3)
        }
        val newRequest4 = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "ADMINISTRATOR"
        )
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc3", newRequest4)
        }
        userToCalendarRepository.save(UserToCalendarEntity(
            user=user1,
            calendar = newCalendar,
            access_type = "MODERATOR"
        ))
        val user4 = UserEntity(
            username = "Адольф4",
            phone = "14884",
            email = "AustrianPainter@nazi.de4",
            tg = "@amTheRealHitler13374",
            password = "BombardiroCrocodillo4",)
        userRepository.save(user4)
        val token4 = TokenEntity(
            token = "abc4",
            user = user4)
        tokenRepository.save(token4)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user4,
            calendar = newCalendar,
            access_type = "ORGANIZER"
        ))
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc4", newRequest4)
        }
        val user5 = UserEntity(
            username = "Bob5",
            tg = "@SIGMABOY5",
            password = "TRALALELOTRALALA5",
            phone = "7896725368275",
            email = "@BOMBOMBINIGUSINI5",
        )
        userRepository.save(user5)
        val token5 = TokenEntity(
            token = "abc5",
            user = user5)
        tokenRepository.save(token5)
        userToCalendarRepository.save(UserToCalendarEntity(
            user=user5,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val user6 = UserEntity(
            username = "Адольф6",
            phone = "14886",
            email = "AustrianPainter@nazi.de6",
            tg = "@amTheRealHitler13376",
            password = "BombardiroCrocodillo6",)
        userRepository.save(user6)
        val token6 = TokenEntity(
            token = "abc6",
            user = user6)
        tokenRepository.save(token6)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user6,
            calendar = newCalendar,
            access_type = "MODERATOR"
        ))
        val newRequest6 = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY5",
            accessType = "ADMINISTRATOR"
        )
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc6", newRequest4)
        }
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc6", newRequest6)
        }
        val user7 = UserEntity(
            username = "Bob7",
            tg = "@SIGMABOY7",
            password = "TRALALELOTRALALA7",
            phone = "7896725368277",
            email = "@BOMBOMBINIGUSINI7",
        )
        userRepository.save(user7)
        val token7 = TokenEntity(
            token = "abc7",
            user = user7)
        tokenRepository.save(token7)
        userToCalendarRepository.save(UserToCalendarEntity(
            user=user7,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val user8 = UserEntity(
            username = "Адольф8",
            phone = "14888",
            email = "AustrianPainter@nazi.de8",
            tg = "@amTheRealHitler13378",
            password = "BombardiroCrocodillo8",)
        userRepository.save(user8)
        val token8 = TokenEntity(
            token = "abc8",
            user = user8)
        tokenRepository.save(token8)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user8,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val newRequest8 = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY7",
            accessType = "MODERATOR"
        )
        shouldThrow<LimitedAccessRightsException> {
            calendarService.manageUsers("abc8", newRequest8)
        }
    }
    @Test
    fun `Изменение прав пользователя, успешное выполнение`() {
        val newRequest = ManageUsersRequest(
            teg = "rest",
            userTg = "@SIGMABOY",
            accessType = "MODERATOR"
        )
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        userToCalendarRepository.save(UserToCalendarEntity(
            user= user,
            calendar = newCalendar,
            access_type = "ADMINISTRATOR"
        ))
        val user2 = UserEntity(
            username = "Bob",
            tg = "@SIGMABOY",
            password = "TRALALELOTRALALA",
            phone = "789672536827",
            email = "@BOMBOMBINIGUSINI",
        )
        userRepository.save(user2)
        val token2 = TokenEntity(
            token = "abc2",
            user = user2)
        tokenRepository.save(token2)
        userToCalendarRepository.save(UserToCalendarEntity(
            user=user2,
            calendar = newCalendar,
            access_type = "VIEWER"
        ))
        calendarService.manageUsers("abc", newRequest)
        userToCalendarRepository.findByUserAndCalendar(user=user2, calendar = newCalendar) shouldBe
                UserToCalendarEntity(id = 2, user=user2, calendar = newCalendar, access_type = newRequest.accessType)
    }

    @Test
    fun `Удаление календаря, календаря с данным тегом не существует`() {
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("PUPUPUPUPU")
        shouldThrow<InvalidTegException> { calendarService.deleteCalendar("abc", newRequest) }
    }

    @Test
    fun `Удаление календаря, календарь с данным тегом неактивен`(){
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=false)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("PUPUPUPUPU")
        shouldThrow<InvalidTegException> { calendarService.deleteCalendar("abc", newRequest)}
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для взаимодейтсвия с данным календарем`(){
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = false,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("rest")
        shouldThrow<PrivateCalendarException> { calendarService.deleteCalendar("abc", newRequest)}
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar, access_type = "DELETED"))
        shouldThrow<PrivateCalendarException> { calendarService.deleteCalendar("abc", newRequest)}
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для изменения данного публичного календаря`(){
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("rest")
        shouldThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("abc", newRequest)}
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar, access_type = "DELETED"))
        shouldThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("abc", newRequest)}
    }

    @Test
    fun `Удаление календаря, у пользователя недостаточно прав для удаления данного календаря`(){
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("rest")
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar, access_type = "VIEWER"))
        shouldThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("abc", newRequest)}
        val newCalendar1 = CalendarEntity(
            calendar_name = "PUPUPU1",
            public = true,
            teg = "rest1",
            description = "I AM TIRED1",
            active=true)
        calendarRepository.save(newCalendar1)
        val newRequest1 = DeleteCalendarRequest("rest1")
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar1, access_type = "MODERATOR"))
        shouldThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("abc", newRequest1)}
        val newCalendar2 = CalendarEntity(
            calendar_name = "PUPUPU2",
            public = true,
            teg = "rest2",
            description = "I AM TIRED2",
            active=true)
        calendarRepository.save(newCalendar2)
        val newRequest2 = DeleteCalendarRequest("rest2")
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar2, access_type = "ORGANIZER"))
        shouldThrow<LimitedAccessRightsException> { calendarService.deleteCalendar("abc", newRequest2)}
    }

    @Test
    fun `Удаление календаря, успешное выполнение`(){
        val user = UserEntity(
            username = "Адольф",
            phone = "1488",
            email = "AustrianPainter@nazi.de",
            tg = "@amTheRealHitler1337",
            password = "BombardiroCrocodillo",)
        userRepository.save(user)
        val token = TokenEntity(
            token = "abc",
            user = user)
        tokenRepository.save(token)
        val newCalendar = CalendarEntity(
            calendar_name = "PUPUPU",
            public = true,
            teg = "rest",
            description = "I AM TIRED",
            active=true)
        calendarRepository.save(newCalendar)
        val newRequest = DeleteCalendarRequest("rest")
        userToCalendarRepository.save(UserToCalendarEntity(user=user, calendar = newCalendar, access_type = "ADMINISTRATOR"))
        calendarService.deleteCalendar("abc", newRequest)
        calendarRepository.findByTeg(newCalendar.teg) shouldBe CalendarEntity(
            id=1,
            calendar_name = newCalendar.calendar_name,
            public = newCalendar.public,
            teg = newCalendar.teg,
            description = newCalendar.description,
            active=false)
    }
}