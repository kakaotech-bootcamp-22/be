package com.spring.be.user.repository;

import com.spring.be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;

public interface UserRepository extends JpaRepository<User, Long> {
    User findBySocialId(BigInteger socialId);
    @Query("SELECT u.userId FROM User u WHERE u.socialId = :socialId")
    Long findUserIdBySocialId(@Param("socialId") BigInteger socialId);
}
