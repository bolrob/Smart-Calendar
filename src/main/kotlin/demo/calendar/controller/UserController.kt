package demo.calendar.controller

import demo.calendar.dto.AuthorizeRequest
import demo.calendar.dto.ManageRequest
import demo.calendar.service.UserService
import demo.calendar.dto.SingUpRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    val userService: UserService
) {
    @PostMapping("/register")
    fun registerUser(@RequestBody request: SingUpRequest) = userService.registerUser(request)

    @PostMapping("/authorize")
    fun authorizeUser(@RequestBody request: AuthorizeRequest) = userService.authorizeUser(request)

    @PutMapping("/manage")
    fun manageUser(@RequestParam("token") token: String, @RequestBody request: ManageRequest) = userService.manageUser(token, request)

    @PutMapping("/logout")
    fun logOut(@RequestParam("token") token: String) = userService.logout(token)
}