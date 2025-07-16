package demo.calendar.service

import demo.calendar.entity.TokenEntity
import demo.calendar.entity.UserEntity
import demo.calendar.exception.NotValidTokenException
import demo.calendar.repository.TokenRepository
import java.util.*

class TokenService(
    private val tokenRepository: TokenRepository
) {
    fun tokenIsValid(token: TokenEntity?) {
        if (token == null){
            throw NotValidTokenException("No user exists with such token")
        }
        if (token.revoked){
            throw NotValidTokenException("User's token has been already revoked")
        }
    }

    fun createToken(user: UserEntity): String {
        val token = TokenEntity(
            token = UUID.randomUUID().toString(),
            user = user
        )
        tokenRepository.save(token)
        return token.token
    }

    fun findByToken(token: String) = tokenRepository.findByToken(token)

    fun revokeToken(tEntity: TokenEntity) {
        tokenRepository.save(TokenEntity(
            id = tEntity.id,
            token = tEntity.token,
            user = tEntity.user,
            revoked = true
        ))
    }
}