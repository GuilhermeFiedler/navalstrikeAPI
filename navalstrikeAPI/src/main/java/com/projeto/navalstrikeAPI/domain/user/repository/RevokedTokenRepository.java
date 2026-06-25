package com.projeto.navalstrikeAPI.domain.user.repository;

import com.projeto.navalstrikeAPI.domain.user.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {
    boolean existsByTokenId(String tokenId);
}
