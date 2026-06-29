package com.projeto.navalstrikeAPI.domain.coordinate.board.repository;

import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
}
