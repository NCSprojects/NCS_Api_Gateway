package server.apigateway.filiter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.stereotype.Component

import org.springframework.web.server.ServerWebExchange


import org.springframework.core.env.Environment
import reactor.core.publisher.Mono

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import server.apigateway.dto.UserValidResponse
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

@Component
class CustomFilter(
    private val env: Environment,
    @LoadBalanced private val webClient: WebClient
) : AbstractGatewayFilterFactory<CustomFilter.Config>(Config::class.java) {

    companion object {
        private val log = LoggerFactory.getLogger(CustomFilter::class.java)
    }

    class Config

    // 토큰 검증 로직 필터
    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request

            // 헤더에 인증 정보가 없는 경우 에러 반환
            if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return@GatewayFilter onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED)
            }

            val authorizationHeader = request.headers[HttpHeaders.AUTHORIZATION]?.get(0) ?: ""
            val jwt = authorizationHeader.replace("Bearer", "").trim()

            // JWT 토큰 검증
            if (!isJwtValid(jwt)) {
                return@GatewayFilter onError(exchange, "Token Is not Valid", HttpStatus.UNAUTHORIZED)
            }

            // JWT에서 id 추출
            val id = extractIdFromJwt(jwt)
            if (id != null) {
                // 사용자 존재 여부 확인
                return@GatewayFilter webClient.get()
                    .uri("http://localhost:8000/users/verify/{id}", id) // 사용자 서비스 URL로 호출
                    .retrieve()
                    .onStatus({ it != HttpStatus.OK }, { Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")) })
                    .bodyToMono(UserValidResponse::class.java)
                    .flatMap { userExists ->
                        if (userExists.valid) {
                            chain.filter(exchange) // 사용자 존재하면 계속 진행
                        } else {
                            onError(exchange, "User not found", HttpStatus.UNAUTHORIZED) // 사용자 없으면 401 반환
                        }
                    }
                    .onErrorResume {ex->
                        log.error("Error during user validation: ${ex.message}", ex)
                        onError(exchange, "User Service validation failed", HttpStatus.UNAUTHORIZED) // 사용자 검증 실패
                    }
            } else {
                return@GatewayFilter onError(exchange, "Invalid JWT payload", HttpStatus.UNAUTHORIZED)
            }
        }
    }

    // JWT 토큰 유효성 검사
    fun isJwtValid(jwt: String): Boolean {
        return try {
            // env에서 비밀 키 가져오기
            val base64EncodedSecret = env.getProperty("jwt.secret") ?: "YTRmZWFjYWNmYjFkY2FkZWYxY2RkYmFkY2FkZmQwZjM0Zjd"

            if (base64EncodedSecret.isEmpty()) {
                throw IllegalStateException("JWT secret key is missing")
            }
            // Base64 인코딩된 비밀 키를 디코딩하여 SecretKey 생성
            val secretKey: SecretKey = Keys.hmacShaKeyFor(base64EncodedSecret.toByteArray(StandardCharsets.UTF_8))

            // JwtParserBuilder를 사용하여 JWT를 검증
            val jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)

            // JWT가 유효한 경우
            val isValid = !jws.payload.isNullOrEmpty()
            if (!isValid) {
                log.error("JWT has an invalid subject or is expired")
            }
            return isValid
        } catch (ex: JwtException) {
            // JWT 검증 예외 처리
            log.error("JWT validation failed: ${ex.message}", ex)
            return false
        } catch (ex: Exception) {
            // 일반적인 예외 처리
            log.error("Error occurred while validating JWT: ${ex.message}", ex)
            return false
        }
    }

    // JWT에서 'id' 추출
    fun extractIdFromJwt(jwt: String): Int? {
        return try {
            val base64EncodedSecret = env.getProperty("jwt.secret") ?: "YTRmZWFjYWNmYjFkY2FkZWYxY2RkYmFkY2FkZmQwZjM0Zjd"
            val secretKey: SecretKey = Keys.hmacShaKeyFor(base64EncodedSecret.toByteArray(StandardCharsets.UTF_8))

            val jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)

            val payload = jws.payload
            // Claims 객체인 경우
            val claims = payload as Claims
            val randomId = claims["randomId"] as? Integer
            randomId?.toInt()

        } catch (ex: Exception) {
            null
        }
    }

    // 에러 처리 담당
    private fun onError(exchange: ServerWebExchange, err: String, httpStatus: HttpStatus): Mono<Void> {
        val response: ServerHttpResponse = exchange.response
        response.statusCode = httpStatus

        log.error(err)
        return response.setComplete()
    }
}