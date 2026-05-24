package com.alexey.tabgenerator.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности приложения.
 * Настраивает правила доступа к эндпоинтам, обработку сессий и JWT фильтр.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Настройка цепочки фильтров безопасности (SecurityFilterChain).
     * Определяет:
     * - CSRF защиту (отключена),
     * - политику сессий (stateless),
     * - обработку ошибок авторизации,
     * - правила доступа к эндпоинтам,
     * - добавление JWT фильтра.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            // Отключение CSRF, так как API stateless
            .csrf(AbstractHttpConfigurer::disable)
            // Stateless сессии: сервер не хранит состояние пользователя
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Обработка ошибок авторизации
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Необходима авторизация");
                })
            )
            // Настройка прав доступа к эндпоинтам
            .authorizeHttpRequests(auth -> auth
                // Разрешить preflight запросы
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                // Доступно без авторизации
                .requestMatchers(HttpMethod.POST,
                    "/auth/register",
                    "/auth/login",
                    "/auth/recover-password"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/genres",
                    "/genres/*"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/tabs/generate"
                ).permitAll()
                // Доступ только для авторизованных пользователей
                .requestMatchers(
                    "/users/**",
                    "/tabs/**"
                ).authenticated()
                // Все остальные запросы открыты
                .anyRequest().permitAll()
            )
            // Добавление JWT фильтра перед стандартным фильтром аутентификации
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Бин для кодирования паролей пользователей.
     * Используется BCrypt для безопасного хранения паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}