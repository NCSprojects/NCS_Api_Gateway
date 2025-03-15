package server.apigateway

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import server.apigateway.filter.ApiResponseWrappingFilter
//import server.apigateway.dto.toJson
import server.apigateway.filter.CustomFilter

@Configuration
class RoutesConfig(
    private val customFilter: CustomFilter,
    private val apiResponseWrappingFilter: ApiResponseWrappingFilter
) {
    @Bean
    fun routeLocator(builder: RouteLocatorBuilder) = builder.routes {
        route("auth") {
            path("/auth/**")
            filters {
                // 응답 Wrapping 적용
                filter(apiResponseWrappingFilter.apply())
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
                filter(apiResponseWrappingFilter.apply())
            }
            uri("lb://user")
        }
        route("reservation") {
            path("/reservation/**")
            filters {
                filter(customFilter.apply(CustomFilter.Config()))
                filter(apiResponseWrappingFilter.apply())
            }
            uri("lb://reservation")
        }
        route("content") {
            path("/contents/**")
            filters {
                filter(customFilter.apply(CustomFilter.Config()))
                filter(apiResponseWrappingFilter.apply())
            }
            uri("lb://content")
        }
        route("stats") {
            path("/stats/**")
            filters {
                filter(customFilter.apply(CustomFilter.Config()))
            }
            uri("lb://stats")
        }
    }
}