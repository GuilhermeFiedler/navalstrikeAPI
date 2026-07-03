package com.projeto.navalstrikeAPI.domain.match.entity;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.board.entity.Board;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "board_player_1_id")
    private Board boardPlayer1;
    @ManyToOne
    @JoinColumn(name = "board_player_2_id")
    private Board boardPlayer2;

    @OneToOne
    @JoinColumn(name = "player_1")
    private User player1;

    @OneToOne
    @JoinColumn(name = "player_2")
    private User player2;

    @ManyToOne
    @JoinColumn(name = "current_turn_id")
    private User currentTurn;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
