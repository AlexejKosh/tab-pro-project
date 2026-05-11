package com.alexey.tabgenerator.repository;

import com.alexey.tabgenerator.entity.Genre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с жанрами.
 * Обеспечивает стандартные CRUD-операции с жанрами.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

}