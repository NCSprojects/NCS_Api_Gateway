package server.apigateway

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    @LoadBalanced
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl("http://localhost")  // 기본 URL 설정
            .build()
    }
}
