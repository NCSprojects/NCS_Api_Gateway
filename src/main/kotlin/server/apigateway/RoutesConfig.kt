package server.apigateway

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.apigateway.dto.ApiResponse
//import server.apigateway.dto.toJson
import server.apigateway.filiter.CustomFilter

@Configuration
class RoutesConfig(private val customFilter: CustomFilter) {
    @Bean
    fun routeLocator(builder: RouteLocatorBuilder) = builder.routes {
        route("auth") {
            path("/auth/**")
            filters {
                // 응답 Wrapping 적용
                modifyResponseBody(String::class.java, String::class.java) { exchange, originalBody ->
                    val wrappedResponse = ApiResponse.success(data = originalBody)

                    // 응답 Content-Type을 JSON으로 설정
                    exchange.response.headers[HttpHeaders.CONTENT_TYPE] = "application/json"

                    Mono.just(wrappedResponse.toJson())
                }
            }
            uri("lb://auth")
        }
        route("user-verify") {
            order(0)
            path("/users/verify/**")
            uri("lb://user")
        }
        route("user-general") {
            order(1)
            path("/users/**")
            filters {
                filter(customFilter.apply(CustomFilter.Config()))
                modifyResponseBody(String::class.java, String::class.java) { exchange, originalBody ->
                    val wrappedResponse = ApiResponse.success(data = originalBody)
                    exchange.response.headers[HttpHeaders.CONTENT_TYPE] = "application/json"
                    Mono.just(wrappedResponse.toJson())
                }
            }
            uri("lb://user")
        }
        route("reservation") {
            path("/reservation/**")
            uri("lb://reservation")
        }
        route("content") {
            path("/contents/**")
            uri("lb://content")
        }
    }
}