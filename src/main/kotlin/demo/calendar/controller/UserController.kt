package demo.calendar.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    val userService: UserService
) {
    @PostMapping("/register")
    fun regUser(@RequestBody request: SignUpRequest) = userService.registerUser(request)

    @PostMapping("/authorize")
    fun authUser(@RequestBody request: AuthorizeRequest) = userService.authUser(request)

    @PutMapping("/update")
    fun updUser(@RequestBody request: SignUpRequest, @RequestParam("token") token: String) = userService.updateUser(request, token)
}