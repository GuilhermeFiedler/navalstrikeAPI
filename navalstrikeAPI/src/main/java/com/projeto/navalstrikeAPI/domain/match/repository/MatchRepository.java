package com.projeto.navalstrikeAPI.domain.match.repository;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByStatus(GameStatus status);
    boolean existsByCode(String code);
    Optional<Match> findByCode(String code);

    @Query("SELECT m FROM Match m WHERE m.status = 'FINISHED' AND (m.player1 = :player OR m.player2 = :player) ORDER BY m.finishedAt DESC")
    List<Match> findFinishedByPlayer(User player);
}
