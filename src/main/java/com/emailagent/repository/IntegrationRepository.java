package com.emailagent.repository;

import com.emailagent.domain.entity.Integration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationRepository extends JpaRepository<Integration, Long> {

    Optional<Integration> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
}
