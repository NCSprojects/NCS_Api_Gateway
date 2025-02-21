package server.apigateway

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.netty.http.client.HttpClient
import org.springframework.cloud.gateway.config.HttpClientCustomizer

@SpringBootApplication
class ApiGatewayApplication
{
//    @Bean
//    fun httpClientCustomizer(): HttpClientCustomizer {
//        return HttpClientCustomizer { httpClient: HttpClient ->
//            httpClient
//                .resolver(DefaultAddressResolverGroup.INSTANCE) // ✅ Netty 기본 DNS Resolver 설정
//                .doOnResponse { response: HttpClientResponse, _, _ ->
//                    if (response.status().code() >= 400) {
//                        println("✅ Netty HttpClient - 오류 감지: ${response.status()}")
//                    }
//                }
//                .mapResponse { response ->
//                    if (response.status().code() >= 400) {
//                        println("✅ Netty HttpClient - 응답 변환 실행")
//                        val errorResponse = ApiResponse.error(
//                            response.status().code(),
//                            "API Gateway Error",
//                            "Service responded with error"
//                        )
//                        val jsonResponse = Json.encodeToString(errorResponse)
//
//                        return@mapResponse response
//                            .status(HttpResponseStatus.OK) // ✅ 모든 오류 응답을 200 OK로 변경
//                            .sendString(Mono.just(jsonResponse), StandardCharsets.UTF_8)
//                    } else {
//                        response
//                    }
//                }
//        }
//    }
}

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
