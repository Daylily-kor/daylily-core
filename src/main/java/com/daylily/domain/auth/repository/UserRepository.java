package com.daylily.domain.auth.repository;

import com.daylily.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // email은 비어있을 수 있으므로, 유니크 값인 githubId로 조회
    Optional<User> findByGithubId(Integer githubId);
}
