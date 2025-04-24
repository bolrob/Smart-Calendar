//package demo.calendar.controller
//
//import demo.calendar.dto.*
//import demo.calendar.service.CalendarService
//import org.springframework.web.bind.annotation.*
//import org.yaml.snakeyaml.events.Event
//
//@RestController
//@RequestMapping("/api/v1/calendar")
//class CalendarController(
//    val calendarService: CalendarService
//) {
//    @PostMapping("/createCalendar")
//    fun createCalendar(@RequestParam("token") token: String, @RequestBody request: CreateCalendarRequest) = calendarService.createCalendar(token, request)
//
//    @PutMapping("/manageUsers")
//    fun manageUsers(@RequestParam("token") token : String, @RequestBody request: ManageUsersRequest) = calendarService.manageUser(token, request)
//
//    @PutMapping("/manageCalendar")
//    fun manageCalendar(@RequestParam("token") token : String, @RequestBody request: ManageCalendarRequest) = calendarService.manageCalendar(token, request)
//
//    @PostMapping("/createEvent")
//    fun createEvent(@RequestParam("token") token : String, @RequestBody request: CreateEventRequest) = calendarService.createEvent(token, request)
//
//    @PutMapping("/manageEvent")
//    fun manageEvent(@RequestParam("token") token: String, @RequestBody request: ManageEventRequest) = calendarService.manageEvent(token, request)
//
//    @PostMapping("/reactEvent")
//    fun react(@RequestParam("token") token : String, @RequestBody request: ReactEventRequest) = calendarService.reactEvent(token, request)
//}