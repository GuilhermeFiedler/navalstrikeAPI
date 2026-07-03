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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = null;


            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }


            if (token == null) {
                token = extractTokenFromHandshakeCookies(accessor);
            }

            if (token == null) {
                throw new IllegalArgumentException("Token JWT ausente");
            }

            try {
                var decoded = jwtService.validateToken(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        UUID.fromString(decoded.getSubject()), null, new ArrayList<>());
                accessor.setUser(auth);
            } catch (Exception e) {
                throw new IllegalArgumentException("Token JWT inválido: " + e.getMessage());
            }
        }

        return message;
    }

    @SuppressWarnings("unchecked")
    private String extractTokenFromHandshakeCookies(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) return null;


        Object cookieHeader = sessionAttributes.get("cookie");
        if (cookieHeader == null) return null;

        String cookies = cookieHeader.toString();
        for (String part : cookies.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("token=")) {
                return trimmed.substring(6);
            }
        }
        return null;
    }
}
