package server.apigateway

import io.netty.resolver.DefaultAddressResolverGroup
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.netty.http.client.HttpClient
import org.springframework.cloud.gateway.config.HttpClientCustomizer

@SpringBootApplication
class ApiGatewayApplication
{}

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
