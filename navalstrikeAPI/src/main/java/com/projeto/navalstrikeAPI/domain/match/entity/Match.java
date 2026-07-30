package com.projeto.navalstrikeAPI.domain.match.entity;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name ="matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_player_1_id")
    private Board boardPlayer1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_player_2_id")
    private Board boardPlayer2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_1")
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_2")
    private User player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_turn_id")
    private User currentTurn;

    @Column(unique = true, nullable = false, length = 6)
    private String code;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(nullable = false)
    private boolean forfeit = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
