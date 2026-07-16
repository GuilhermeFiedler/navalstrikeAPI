package com.projeto.navalstrikeAPI.infra.websocket;

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
    private final DisconnectTimerService disconnectTimerService;

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

        if (sessionRegistry.findSessionByPlayerAndMatch(playerId, matchId).isPresent()) {
            log.info("Jogador {} já reconectou à partida {} com nova sessão, timer não iniciado", playerId, matchId);
            return;
        }

        disconnectTimerService.startTimer(matchId, playerId);
        log.info("Jogador {} desconectou da partida {} - timer de reconexão iniciado", playerId, matchId);
    }
}
