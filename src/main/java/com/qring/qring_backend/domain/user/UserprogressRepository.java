package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserprogressRepository extends JpaRepository<Userprogress, Long> {

    @Query("SELECT AVG(up.progressRate) FROM Userprogress up WHERE up.user.userId = :userId")
    Double findAverageProgressRate(@Param("userId") Long userId);

    @Query("SELECT COUNT(up) FROM Userprogress up WHERE up.user.userId = :userId AND up.progressRate = 100")
    long countCompletedStories(@Param("userId") Long userId);
}
