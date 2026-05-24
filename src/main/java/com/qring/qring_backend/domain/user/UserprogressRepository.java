package com.qring.qring_backend.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Userprogress 조회: 평균 진도율, 완료(100%) 스토리 개수. */
public interface UserprogressRepository extends JpaRepository<Userprogress, Long> {

    /** 사용자가 진행 중인 모든 콘텐츠의 평균 진도율 (없으면 null). */
    @Query("SELECT AVG(up.progressRate) FROM Userprogress up WHERE up.user.userId = :userId")
    Double findAverageProgressRate(@Param("userId") Long userId);

    /** progressRate=100 인 콘텐츠 개수 (= 완료한 스토리 수). */
    @Query("SELECT COUNT(up) FROM Userprogress up WHERE up.user.userId = :userId AND up.progressRate = 100")
    long countCompletedStories(@Param("userId") Long userId);
}
