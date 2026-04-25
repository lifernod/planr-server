package org.niikage.planr.configuration.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.niikage.planr.features.authentication.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono

@Profile("prod")
@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration(
    private val authenticationService: AuthenticationService,
) {
    private val authorizationHeader = "Authorization"
    private val authorizationPrefix = "Bearer "

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(
                    "/api/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                it.anyExchange().authenticated()
            }
            .addFilterAt(authenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun authenticationFilter(): WebFilter {
        return WebFilter { exchange, chain ->
            val header = exchange
                .request
                .headers
                .getFirst(authorizationHeader)

            if (header != null && header.startsWith(authorizationPrefix)) {
                val token = header.removePrefix(authorizationPrefix)

                try {
                    val user = authenticationService.extractUser(token)
                    val auth = UsernamePasswordAuthenticationToken(
                        user.id.value,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                    )

                    return@WebFilter chain.filter(exchange)
                        .contextWrite { ReactiveSecurityContextHolder.withAuthentication(auth) }
                } catch (_: ExpiredJwtException) {
                    return@WebFilter unauthorized(exchange, "Токен устарел. Войдите в аккаунт заново")
                } catch (_: JwtException) {
                    return@WebFilter unauthorized(exchange, "Неверный токен доступа")
                }
            }

            chain.filter(exchange)
        }
    }

    fun unauthorized(exchange: ServerWebExchange, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED

        val buffer = response.bufferFactory()
            .wrap("""{"error":"$message"}""".toByteArray())

        println(message)

        return response.writeWith(Mono.just(buffer))
    }
}