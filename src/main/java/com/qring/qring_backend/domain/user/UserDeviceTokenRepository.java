package com.qring.qring_backend.domain.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** 기기 토큰 조회·정리. 발송 대상 토큰 조회는 사용자 여러 명을 한 번에 가져온다 (스케줄러용). */
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Optional<UserDeviceToken> findByToken(String token);

    List<UserDeviceToken> findAllByUserId(Long userId);

    List<UserDeviceToken> findAllByUserIdIn(Collection<Long> userIds);

    /** 로그아웃: 본인 소유 토큰만 지운다 (다른 계정 토큰을 실수로 지우지 않게 userId 조건 포함). */
    @Transactional
    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.userId = :userId AND t.token = :token")
    int deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);

    /** FCM 이 UNREGISTERED / INVALID_ARGUMENT 로 거부한 토큰 일괄 삭제. */
    @Transactional
    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.token IN :tokens")
    int deleteAllByTokenIn(@Param("tokens") Collection<String> tokens);

    /** 회원 탈퇴: 사용자의 토큰 전부 삭제 (UserWithdrawalService 전용, 서비스 트랜잭션 안에서 호출). */
    @Modifying
    @Query("DELETE FROM UserDeviceToken t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
