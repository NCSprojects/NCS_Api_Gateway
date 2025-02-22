package server.apigateway.filter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import server.apigateway.dto.ApiResponse
import server.apigateway.dto.toJson

@Component
class ApiResponseWrappingFilter(
    private val modifyResponseBodyGatewayFilterFactory: ModifyResponseBodyGatewayFilterFactory
) {
    fun apply(): GatewayFilter {
        return modifyResponseBodyGatewayFilterFactory.apply(
            ModifyResponseBodyGatewayFilterFactory.Config()
                .setRewriteFunction(String::class.java, String::class.java) { exchange, originalBody ->
                    val jsonElement: JsonElement = Json.parseToJsonElement(originalBody)
                    val wrappedResponse = ApiResponse.success(data = jsonElement)

                    // 응답 Content-Type을 JSON으로 설정
                    exchange.response.headers[HttpHeaders.CONTENT_TYPE] = "application/json"

                    Mono.just(wrappedResponse.toJson())
                }
        )
    }
}