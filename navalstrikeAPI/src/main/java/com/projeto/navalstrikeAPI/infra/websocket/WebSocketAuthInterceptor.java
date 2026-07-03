package com.projeto.navalstrikeAPI.infra.websocket;

import com.projeto.navalstrikeAPI.infra.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token JWT ausente ou inválido");
            }

            try {
                var decoded = jwtService.validateToken(token.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(
                        UUID.fromString(decoded.getSubject()), null, new ArrayList<>());
                accessor.setUser(auth);
            } catch (Exception e) {
                throw new IllegalArgumentException("Token JWT inválido: " + e.getMessage());
            }
        }

        return message;
    }
}
