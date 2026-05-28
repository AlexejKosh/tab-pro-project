package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.User;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для {@link UserRepository}.
 *
 * Проверяют работу стандартных методов JPA:
 * - поиск пользователей
 * - сохранение нового пользователя
 * - удаление пользователя
 *
 * А также пользовательские методы репозитория:
 * - поиск пользователей по username или email
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Поиск пользователя по id: успех")
    void findById_success() {
        Optional<User> user = userRepository.findById(3L);

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("goodTester");
    }

    @Test
    @DisplayName("Поиск пользователя по id: несуществующий id")
    void findById_fail_notFound() {
        Optional<User> user = userRepository.findById(999L);

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Сохранение нового пользователя: успех")
    void save_success() {
        User user = User.builder()
            .username("new_user")
            .email("new_user@mail.com")
            .passwordHash("password_hash")
            .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();

        Optional<User> fromDb = userRepository.findById(savedUser.getId());

        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getUsername()).isEqualTo("new_user");
    }

    @Test
    @DisplayName("Удаление пользователя по id: успех")
    void deleteById_success() {
        userRepository.deleteById(6L);

        Optional<User> user = userRepository.findById(6L);

        assertThat(user).isEmpty();
    }


    @Test
    @DisplayName("Поиск пользователя по username: успех")
    void findByUsername_success() {
        Optional<User> user = userRepository.findByUsername("testUser1");

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("testuser1@mail.ru");
    }

    @Test
    @DisplayName("Поиск пользователя по username: несуществующий username")
    void findByUsername_fail_notFound() {
        Optional<User> user = userRepository.findByUsername("unknown");

        assertThat(user).isEmpty();
    }


    @Test
    @DisplayName("Поиск пользователя по email: успех")
    void findByEmail_success() {
        Optional<User> user = userRepository.findByEmail("alexeytest@mail.ru");

        assertThat(user).isPresent();
        assertThat(user.get().getUsername()).isEqualTo("alexeyTest");
    }

    @Test
    @DisplayName("Поиск пользователя по email: несуществующий email")
    void findByEmail_fail_notFound() {
        Optional<User> user = userRepository.findByEmail("not_exists@mail.com");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователя по username или email: успех (username)")
    void findByUsernameOrEmail_success_username() {
        Optional<User> user =
            userRepository.findByUsernameOrEmail("alexeyTest", "something@mail.com");

        assertThat(user).isPresent();
    }

    @Test
    @DisplayName("Поиск пользователя по username или email: успех (email)")
    void findByUsernameOrEmail_success_email() {
        Optional<User> user =
            userRepository.findByUsernameOrEmail("something", "testuser1@mail.ru");

        assertThat(user).isPresent();
    }

    @Test
    @DisplayName("Поиск пользователя по username или email: пустой результат")
    void findByUsernameOrEmail_success_empty() {
        Optional<User> user =
            userRepository.findByUsernameOrEmail("none", "none@mail.com");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования по username: существует")
    void existsByUsername_true() {
        boolean exists = userRepository.existsByUsername("testUser1");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверка существования по username: не существует")
    void existsByUsername_false() {
        boolean exists = userRepository.existsByUsername("unknown");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Проверка существования по email: существует")
    void existsByEmail_true() {
        boolean exists = userRepository.existsByEmail("testuser1@mail.ru");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверка существования по email: не существует")
    void existsByEmail_false() {
        boolean exists = userRepository.existsByEmail("not_exists@mail.com");

        assertThat(exists).isFalse();
    }
}