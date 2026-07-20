package com.projeto.navalstrikeAPI.domain.match.dto;

import com.projeto.navalstrikeAPI.common.enums.GameStatus;
import com.projeto.navalstrikeAPI.domain.board.dto.BoardView;

import java.util.UUID;

public record MatchResponse(
        UUID id,
        GameStatus status,
        UUID currentTurn,
        String myName,
        String opponentName,
        String mySkinSlug,
        String opponentSkinSlug,
        BoardView myBoard,
        BoardView opponentBoard
) {
}
