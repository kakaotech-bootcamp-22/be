package com.spring.be.user.repository;

import com.spring.be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface UserRepository extends JpaRepository<User, Long> {
    User findBySocialId(BigInteger socialId);
}
