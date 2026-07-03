package com.projeto.navalstrikeAPI.domain.match.repository;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByStatus(GameStatus status);
}
