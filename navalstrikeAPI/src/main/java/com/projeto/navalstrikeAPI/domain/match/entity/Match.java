package com.projeto.navalstrikeAPI.domain.match.entity;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name ="Match")
@Getter
@Setter
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    GameStatus status;

    @ManyToOne
    @JoinColumn(name = "board_player_1_id")
    Board boardPlayer1;
    @ManyToOne
    @JoinColumn(name = "board_player_2_id")
    Board boardPlayer2;

}
