package com.projeto.navalstrikeAPI.infra.security;

import com.projeto.navalstrikeAPI.domain.user.entity.RevokedToken;
import com.projeto.navalstrikeAPI.domain.user.repository.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.exxpiration-hours:2}")
    private int expirationHours;

    private final RevokedTokenRepository revokedTokenRepository;

    public JwtService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public String generateToken(UUID userId, String email, String name){
        return JWT.create()
                .withIssuer("navalstrike-api")
                .withSubject(userId.toString())
                .withClaim("email", email)
                .withClaim("name", name)
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(expirationInstant())
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token){
        var decoded = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("navalstrike-api")
                .build()
                .verify(token);
    }

    public void revokeToken(String token) {
        var decoded = JWT.decode(token);
        var revoked = new RevokedToken();
        revoked.setTokenId(decoded.getId());
        revoked.setExpiresAt(decoded.getExpiresAtAsInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
        revokedTokenRepository.save(revoked);
    }

    private Instant expirationInstant() {
        return LocalDateTime.now().plusHours(expirationHours).toInstant(ZoneOffset.of("-03:00"));
    }
}
