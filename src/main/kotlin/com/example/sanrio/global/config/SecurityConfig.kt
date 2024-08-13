package com.example.sanrio.global.config

import com.example.sanrio.global.jwt.CustomAuthenticationEntryPoint
import com.example.sanrio.global.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint
) {
    // 별도의 인증 과정 없이 접근할 수 있는 URI
    private val allowedUrls = arrayOf(
        "/favicon.ico",
        "/h2-console/**",
        "/signup/**",
        "/login"
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .csrf { it.disable() }
            .cors { it.disable() }
            .headers { it.frameOptions { foc -> foc.disable() } }
            .authorizeHttpRequests {
                it.requestMatchers(*allowedUrls).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .build()
    }
}