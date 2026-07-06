package com.projeto.navalstrikeAPI.infra.websocket;

import com.projeto.navalstrikeAPI.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final WebSocketSessionRegistry sessionRegistry;
    private final MatchService matchService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        if (!sessionRegistry.isRegistered(sessionId)) {
            return;
        }

        UUID matchId = sessionRegistry.getMatchId(sessionId).orElse(null);
        UUID playerId = sessionRegistry.getPlayerId(sessionId).orElse(null);

        sessionRegistry.unregister(sessionId);

        if (matchId == null || playerId == null) {
            return;
        }

        try {
            matchService.forfeit(matchId, playerId);
            log.info("Forfeit automático: jogador {} desconectou da partida {}", playerId, matchId);
        } catch (Exception e) {
            log.debug("Forfeit automático ignorado para partida {}: {}", matchId, e.getMessage());
        }
    }
}
