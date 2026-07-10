package com.projeto.navalstrikeAPI.domain.match.dto;

import java.time.Instant;
import java.util.UUID;

public record MatchListResponse(UUID id, String hostName, String code, Instant createdAt) {}
