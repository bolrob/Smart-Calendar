package demo.calendar.controller

import demo.calendar.dto.*
import demo.calendar.service.CalendarService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/calendar")
class CalendarController(
    val calendarService: CalendarService
) {
    @PostMapping("/createCalendar")
    fun createCalendar(@RequestParam("token") token: String, @RequestBody request: CreateCalendarRequest) = calendarService.createCalendar(token, request)

    @PutMapping("/manageUsers")
    fun manageUsers(@RequestParam("token") token : String, @RequestBody request: ManageUsersRequest) = calendarService.manageUsers(token, request)

    @PutMapping("/manageCalendar")
    fun manageCalendar(@RequestParam("token") token : String, @RequestBody request: ManageCalendarRequest) = calendarService.manageCalendar(token, request)

    @PutMapping("/deleteCalendar")
    fun deleteCalendar(@RequestParam("token") token : String, @RequestBody request: DeleteCalendarRequest) = calendarService.deleteCalendar(token, request)

    @GetMapping("/getCalendars")
    fun getCalendars(@RequestParam("token") token : String, @RequestParam("page") page : Int, @RequestParam("size") size : Int, @RequestParam("sortBy") sortBy : String?, @RequestParam("type") type : ManageUsersRequest.AccessType) = calendarService.getCalendars(token, page, size, sortBy, type)
}