package demo.calendar.service

import demo.calendar.dto.*
import demo.calendar.entity.EventEntity
import demo.calendar.entity.ReactionEntity
import demo.calendar.exception.*
import demo.calendar.repository.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.toList


@Component
class EventService(
    private val userToCalendarRepository: UserToCalendarRepository,
    private val calendarRepository: CalendarRepository,
    private val eventRepository: EventRepository,
    private val tokenRepository: TokenRepository,
    private val userService: UserService,
    private val reactionRepository: ReactionRepository,
    private val tokenService: TokenService
) {
    fun validRequest(token: String, teg: String): ValidRequestResponse {
        val tEntity = tokenRepository.findByToken(token)
        tokenService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        val calendar = calendarRepository.findByTeg(teg)
            ?: throw InvalidTegException("Calendar with such teg does not exist")
        if (!calendar.active){
            throw NotActiveCalendarException("This calendar is not active")
        }
        val accessType = userToCalendarRepository.findByUserAndCalendar(user, calendar)?.access_type
        if (accessType == null || accessType == "DELETED"){
            if(!calendar.public){
                throw PrivateCalendarException("This calendar is private, you can't interact with it.")
            }
            throw LimitedAccessRightsException("You do not have access rights to create events in this calendar, you can only view it.")
        }
        return ValidRequestResponse(user, calendar, accessType)
    }

    fun createEvent(token: String, request: EventRequest): EventResponse {
        val tmp = validRequest(token, request.teg)
        val user = tmp.user
        val calendar = tmp.calendar
        val accessType = tmp.accessType
        if (accessType == "VIEWER") throw LimitedAccessRightsException("You do not have access rights to create events in this calendar, you can only view it.")
        val event = EventEntity(
            title = request.event.title,
            description = request.event.description,
            address = request.event.address,
            startTime = request.event.startTime,
            endTime = request.event.endTime,
            user = user,
            calendar = calendar,
            status = request.event.status,
            averageRating = 0.0
        )
        val entity = eventRepository.save(event)
        return request.event.toResponse(entity.id)
    }

    @Transactional
    fun manageEvent(token: String, request: EventRequest): EventResponse {
        val tmp = validRequest(token, request.teg)
        val user = tmp.user
        val calendar = tmp.calendar
        val accessType = tmp.accessType
        if (accessType == "VIEWER") throw LimitedAccessRightsException("You do not have access rights to manage events in this calendar.")
        val oldEvent = eventRepository.findById(request.id).toList()[0] ?: throw WrongIdException("No event with such id exists.")
        if (oldEvent.user != user) throw WrongUserException("You do not have access rights to manage this event.")
        val event = EventEntity(
            id = oldEvent.id,
            title = request.event.title,
            description = request.event.description,
            address = request.event.address,
            startTime = request.event.startTime,
            endTime = request.event.endTime,
            user = user,
            calendar = calendar,
            status = request.event.status,
            averageRating = oldEvent.averageRating
        )
        eventRepository.save(event)
        return request.event.toResponse(oldEvent.id)
    }

    @Transactional
    fun react(token: String, request: ReactRequest): Double {
        val tEntity = tokenRepository.findByToken(token)
        tokenService.tokenIsValid(tEntity)
        val user = tEntity!!.user
        val calendar = calendarRepository.findByTeg(request.teg)
            ?: throw InvalidTegException("Calendar with such teg does not exist")
        if (!calendar.active){
            throw NotActiveCalendarException("This calendar is not active")
        }
        val accessType = userToCalendarRepository.findByUserAndCalendar(user, calendar)?.access_type
        if ((accessType == null || accessType == "DELETED") && !calendar.public) throw PrivateCalendarException("This calendar is private, you can't interact with it.")
        val event = eventRepository.findById(request.eventId).toList()[0] ?: throw WrongIdException("No event with such id exists.")
        val oldReaction = reactionRepository.findByEventAndUser(event, user)
        if (oldReaction != null) {
            reactionRepository.save(ReactionEntity(
                id = oldReaction.id,
                user = user,
                event = event,
                reaction = request.reaction
            ))
        }
        else {
            reactionRepository.save(ReactionEntity(
                user = user,
                event = event,
                reaction = request.reaction
            ))
        }
        val reactions = reactionRepository.findByEvent(event)
        val mean = reactions.sumOf { it.reaction } / reactions.size
        eventRepository.save(event.changeRating(mean))
        return request.reaction
    }
}