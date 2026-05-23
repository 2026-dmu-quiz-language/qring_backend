package com.qring.qring_backend.domain.difficulty;

import org.springframework.data.jpa.repository.JpaRepository;

/** DifficultyLevel CRUD. */
public interface DifficultyLevelRepository extends JpaRepository<DifficultyLevel, Integer> {
}
