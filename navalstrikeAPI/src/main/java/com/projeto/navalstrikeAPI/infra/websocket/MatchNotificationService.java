package com.projeto.navalstrikeAPI.infra.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar um evento para todos os inscritos no tópico da partida.
     * Frontend se inscreve em: /topic/match/{matchId}
     */
    public void notify(UUID matchId, MatchEvent event) {
        messagingTemplate.convertAndSend("/topic/match/" + matchId, event);
    }

    public void notifyPlayerJoined(UUID matchId, UUID playerId, String playerName) {
        var event = new MatchEvent(
                MatchEvent.EventType.PLAYER_JOINED,
                matchId,
                playerId,
                Map.of("playerName", playerName)
        );
        notify(matchId, event);
    }

    public void notifyGameStarted(UUID matchId) {
        var event = new MatchEvent(
                MatchEvent.EventType.GAME_STARTED,
                matchId,
                null,
                null
        );
        notify(matchId, event);
    }

    public void notifyAttackResult(UUID matchId, UUID attackerId, int x, int y, boolean hit, boolean sunk, boolean gameOver) {
        var event = new MatchEvent(
                MatchEvent.EventType.ATTACK_RESULT,
                matchId,
                attackerId,
                Map.of(
                        "x", x,
                        "y", y,
                        "hit", hit,
                        "sunk", sunk,
                        "gameOver", gameOver
                )
        );
        notify(matchId, event);
    }

    public void notifyGameOver(UUID matchId, UUID winnerId) {
        var event = new MatchEvent(
                MatchEvent.EventType.GAME_OVER,
                matchId,
                winnerId,
                Map.of("winnerId", winnerId)
        );
        notify(matchId, event);
    }
}
