package com.alexey.tabgenerator.security;

import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.exception.InvalidJwtTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Сервис для работы с JWT (JSON Web Token).
 * Генерация токенов, проверка их валидности и извлечение данных.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    /**
     * Инициализация ключа подписи после создания бина.
     */
    @PostConstruct
    public void init() {
        // Создание HMAC ключа для подписи JWT
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерация JWT для пользователя.
     */
    public String generateToken(User user) {
        log.debug("Генерация JWT для пользователя: username={}", user.getUsername());

        return Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Извлечение username из JWT токена.
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Проверка валидности JWT токена для пользователя.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);

            boolean valid = username.equals(userDetails.getUsername())
                && !getClaims(token).getExpiration().before(new Date());

            if (!valid) {
                log.warn("JWT токен не прошёл проверку для пользователя: username={}", username);
            }

            return valid;

        } catch (Exception e) {
            throw new InvalidJwtTokenException("Ошибка при проверке JWT токена: " + e.getMessage());
        }
    }

    /**
     * Построение объекта аутентификации Spring Security на основе UserDetails.
     */
    public UsernamePasswordAuthenticationToken buildAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
    }

    /**
     * Извлечение Claims (данных) из JWT токена.
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}