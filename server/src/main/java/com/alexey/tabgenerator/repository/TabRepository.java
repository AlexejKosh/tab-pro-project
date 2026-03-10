package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.Tab;
import com.alexey.tabgenerator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с табулатурами.
 * Позволяет получать табулатуры пользователя и стандартные CRUD-операции.
 */
@Repository
public interface TabRepository extends JpaRepository<Tab, Long> {

    List<Tab> findByUser(User user);
}