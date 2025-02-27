package demo.calendar.controller

import org.springframework.web.bind.annotation.*
import org.yaml.snakeyaml.events.Event

@RestController
@RequestMapping("/api/calendar")
class CalendarController(
    val calendarService: CalendarService
) {
    @PostMapping("/newCalendar")
    fun newCalendar(@RequestParam("token") token: String, @RequestBody request: CalendarRequest) = calendarService.newCalendar(token, request)

    @PutMapping("/manageUsers")
    fun manageUsers(@RequestParam("token") token : String, @RequestParam("calendarId") id: Long, @RequestParam("tg") user: String, @RequestParam("operation") work: String) = calendarService.addUser(token, id, user, work)

    @PutMapping("/addEvent")
    fun addEvent(@RequestParam("token") token : String, @RequestParam("calendarId") id: Long, @RequestBody event: Event, @RequestParam("operation") work: String) = calendarService.addEvent(token, id, event, work)

    @PutMapping("/updateEvent")
    fun updateEvent(@RequestParam("token") token : String, @RequestParam("calendarId") id: Long, @RequestParam("eventId") event: Long, @RequestBody newEvent: Event, @RequestParam("operation") work: String) = calendarService.updateEvent(token, id, event, newEvent, work)

    @PutMapping("/deleteEvent")
    fun delEvent(@RequestParam("token") token : String, @RequestParam("calendarId") id: Long, @RequestParam("eventId") event: Long, @RequestParam("operation") work: String) = calendarService.delEvent(token, id, event, work)

    @PostMapping("/reactEvent")
    fun React(@RequestParam("token") token : String, @RequestParam("calendarId") id: Long, @RequestParam("eventId") event: Long, @RequestBody reaction: Reaction) = calendarService.reactEvent(token, id, event, reaction)
}