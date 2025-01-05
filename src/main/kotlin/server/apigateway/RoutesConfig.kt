package server.apigateway

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import server.apigateway.filiter.CustomFilter

@Configuration
class RoutesConfig( private val customFilter: CustomFilter) {
    @Bean
    fun routeLocator(builder: RouteLocatorBuilder) = builder.routes {
        route("auth") {
            // "/auth"로 시작하는 모든 경로를 처리
            path("/auth/**")
            uri("lb://auth")  // NestJS 애플리케이션의 URL (localhost:3000)
        }
        route("users") {
            path("/users/users")
            uri("lb://auth")  // 예시로 실제 사용자 서비스 URI
            filters {
                // CustomFilter를 적용
                filter(customFilter.apply(CustomFilter.Config()))
            }
        }
        route("users-without-filiter") {
            path("/users/**")
            uri("lb://auth")  // 예시로 실제 사용자 서비스 URI
        }
    }
}