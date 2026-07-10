package com.projeto.navalstrikeAPI.domain.user.dto;

import java.util.UUID;

public record AuthResponse(UUID id, String name, String token) {}
