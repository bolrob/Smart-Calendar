package demo.calendar.controller

import demo.calendar.dto.*
import demo.calendar.service.EventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/event")
class EventController(
    val eventService: EventService
) {
    @PostMapping("/createEvent")
    fun createEvent(@RequestParam("token") token : String, @RequestBody request: EventRequest) = eventService.createEvent(token, request)

    @PutMapping("/manageEvent")
    fun manageEvent(@RequestParam("token") token: String, @RequestBody request: EventRequest) = eventService.manageEvent(token, request)

    @PutMapping("/reactEvent")
    fun react(@RequestParam("token") token : String, @RequestBody request: ReactRequest) = eventService.react(token, request)
}