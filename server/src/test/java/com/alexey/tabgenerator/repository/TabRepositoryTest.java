package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.entity.Genre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для {@link TabRepository}.
 *
 * Проверяют работу стандартных методов JPA:
 * - поиск табулатур по id
 * - получение всех табулатур
 * - сохранение новой табулатуры
 * - удаление табулатуры по id
 * 
 * А также пользовательские методы репозитория:
 * - поиск табулатур по пользователю
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TabRepositoryTest {

    @Autowired
    private TabRepository tabRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Test
    @DisplayName("Поиск табулатуры по id: успех")
    void findById_success() {
        Optional<Tab> result = tabRepository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Что-то вроде Битлз");
    }

    @Test
    @DisplayName("Поиск табулатуры по id: несуществующий id")
    void findById_fail_notFound() {
        Optional<Tab> result = tabRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение всех табулатур: успех")
    void findAll_success() {
        List<Tab> result = tabRepository.findAll();

        assertThat(result).hasSize(9);
        assertThat(result)
            .extracting(Tab::getTitle)
            .contains("Что-то вроде Битлз", "В стиле Хэдфилда", "Точно Кинг");
    }

    @Test
    @DisplayName("Сохранение новой табулатуры: успех")
    void save_success() {
        User user = userRepository.findById(1L).orElseThrow();
        Genre genre = genreRepository.findById(3L).orElseThrow();

        Tab newTab = Tab.builder()
            .user(user)
            .genre(genre)
            .title("Новая табулатура")
            .signature("4/4")
            .chordProgression("C-1")
            .musicKey(0)
            .bpm(100)
            .tabData("test")
            .audioUrl("url")
            .build();

        Tab saved = tabRepository.save(newTab);

        assertThat(saved.getId()).isNotNull();

        Optional<Tab> fromDb = tabRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getTitle()).isEqualTo("Новая табулатура");
    }

    @Test
    @DisplayName("Удаление табулатуры по id: успех")
    void deleteById_success() {
        tabRepository.deleteById(1L);

        Optional<Tab> tab = tabRepository.findById(1L);

        assertThat(tab).isEmpty();
    }

    @Test
    @DisplayName("Поиск табулатур по пользователю: успех (непустой список)")
    void findByUser_success() {
        User user = userRepository.findById(1L).orElseThrow();

        List<Tab> result = tabRepository.findByUser(user);

        assertThat(result).hasSize(3);
        assertThat(result)
            .extracting(Tab::getTitle)
            .containsExactlyInAnyOrder("Что-то вроде Битлз", "В стиле Хэдфилда", "Точно Кинг");
    }

    @Test
    @DisplayName("Поиск табулатур по пользователю: успех (пустой список)")
    void findByUser_success_emptyList() {
        User newUser = User.builder()
            .username("temp_user_for_test")
            .email("temp_user_for_test@mail.ru")
            .passwordHash("hash")
            .build();

        User saved = userRepository.save(newUser);

        List<Tab> result = tabRepository.findByUser(saved);

        assertThat(result).isEmpty();
    }
}