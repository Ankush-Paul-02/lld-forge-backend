package com.devmare.lldforge.data.repository;

import com.devmare.lldforge.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(String githubId);

    Optional<User> findByEmail(String email);
}
