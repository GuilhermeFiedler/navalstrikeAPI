package com.projeto.navalstrikeAPI.domain.match.repository;

import com.projeto.navalstrikeAPI.domain.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
