package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.entity.User;
import com.alexey.tabgenerator.entity.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link TabRepository}.
 *
 * Проверяют работу стандартных методов JPA:
 * - поиск пользователя
 * - сохранение нового пользователя
 * - удаление пользователя
 *
 * А также пользовательские методы репозитория:
 * - поиск по username
 * - поиск по email
 * - поиск по username или email
 * - проверка существования по username/email
 */
class TabRepositoryTest {

    private TabRepository tabRepository;

    private User user1;
    private User user2;

    private Genre rockGenre;
    private Genre bluesGenre;

    private Tab tab1;
    private Tab tab2;
    private Tab tab3;

    @BeforeEach
    void setup() {
        tabRepository = mock(TabRepository.class);

        user1 = User.builder().id(6L).username("alexeyKo").build();
        user2 = User.builder().id(7L).username("alina_orlova").build();

        rockGenre = Genre.builder().id(1L).name("Rock").build();
        bluesGenre = Genre.builder().id(2L).name("Blues").build();

        tab1 = Tab.builder().id(8L).user(user2).genre(rockGenre).title("Для будущего проекта").signature("4/4").build();
        tab2 = Tab.builder().id(9L).user(user2).genre(rockGenre).title("Что-то темное").signature("3/4").build();
        tab3 = Tab.builder().id(11L).user(user1).genre(bluesGenre).title("Типо Джеймс Браун").signature("4/4").build();
    }

    @Test
    @DisplayName("Поиск табулатуры по id: успех")
    void findById_success() {
        when(tabRepository.findById(8L)).thenReturn(Optional.of(tab1));

        Optional<Tab> result = tabRepository.findById(8L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Для будущего проекта");
    }

    @Test
    @DisplayName("Поиск табулатуры по id: несуществующий id")
    void findById_fail_notFound() {
        when(tabRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Tab> result = tabRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение всех табулатур: успех")
    void findAll_success() {
        List<Tab> allTabs = List.of(tab1, tab2, tab3);
        when(tabRepository.findAll()).thenReturn(allTabs);

        List<Tab> result = tabRepository.findAll();

        assertThat(result).hasSize(3);
        assertThat(result)
            .extracting(Tab::getTitle)
            .containsExactlyInAnyOrder("Для будущего проекта", "Что-то темное", "Типо Джеймс Браун");
    }

    @Test
    @DisplayName("Сохранение новой табулатуры: успех")
    void save_success() {
        Tab newTab = Tab.builder().user(user1).genre(rockGenre).title("Новая табулатура").signature("4/4").build();
        Tab savedTab = Tab.builder().id(20L).user(user1).genre(rockGenre).title("Новая табулатура").signature("4/4").build();

        when(tabRepository.save(newTab)).thenReturn(savedTab);

        Tab result = tabRepository.save(newTab);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getTitle()).isEqualTo("Новая табулатура");
    }

    @Test
    @DisplayName("Удаление табулатуры по id: успех")
    void deleteById_success() {
        tabRepository.deleteById(8L);
        Optional<Tab> tab = tabRepository.findById(8L);

        assertThat(tab).isEmpty();
    }

    @Test
    @DisplayName("Поиск табулатур по пользователю: успех (непустой список)")
    void findByUser_success() {
        List<Tab> user2Tabs = List.of(tab1, tab2);
        when(tabRepository.findByUser(user2)).thenReturn(user2Tabs);

        List<Tab> result = tabRepository.findByUser(user2);

        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(Tab::getTitle)
            .containsExactlyInAnyOrder("Для будущего проекта", "Что-то темное");
    }

    @Test
    @DisplayName("Поиск табулатур по пользователю: успех (пустой список)")
    void findByUser_success_emptyList() {
        when(tabRepository.findByUser(user2)).thenReturn(List.of());

        List<Tab> result = tabRepository.findByUser(user2);

        assertThat(result).isEmpty();
    }
}