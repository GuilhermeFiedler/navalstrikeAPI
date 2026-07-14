package com.projeto.navalstrikeAPI.domain.skin.controller;

import com.projeto.navalstrikeAPI.domain.skin.dto.EquipSkinRequest;
import com.projeto.navalstrikeAPI.domain.skin.dto.EquippedSkinResponse;
import com.projeto.navalstrikeAPI.domain.skin.dto.SkinPackResponse;
import com.projeto.navalstrikeAPI.domain.skin.service.SkinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/skins")
@RequiredArgsConstructor
public class SkinController {
    private final SkinService skinService;

    @GetMapping
    public List<SkinPackResponse> listAll() {
        return skinService.listAll();
    }

    @PutMapping("/equip")
    public ResponseEntity<Void> equip(@RequestBody EquipSkinRequest request) {
        UUID userId = getUserId();
        skinService.equip(userId, request.skinPackId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/equipped")
    public EquippedSkinResponse getEquipped() {
        UUID userId = getUserId();
        return skinService.getEquipped(userId);
    }

    private UUID getUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}
