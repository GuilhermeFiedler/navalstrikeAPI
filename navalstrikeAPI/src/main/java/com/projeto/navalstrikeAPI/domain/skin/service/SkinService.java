package com.projeto.navalstrikeAPI.domain.skin.service;

import com.projeto.navalstrikeAPI.domain.skin.dto.EquippedSkinResponse;
import com.projeto.navalstrikeAPI.domain.skin.dto.SkinPackResponse;
import com.projeto.navalstrikeAPI.domain.skin.entity.SkinPack;
import com.projeto.navalstrikeAPI.domain.skin.repository.SkinPackRepository;
import com.projeto.navalstrikeAPI.domain.user.entity.User;
import com.projeto.navalstrikeAPI.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkinService {
    private final SkinPackRepository skinPackRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SkinPackResponse> listAll() {
        return skinPackRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void equip(UUID userId, UUID skinPackId) {
        User user = userRepository.findById(userId).orElseThrow();

        if (skinPackId == null) {
            user.setEquippedSkinPack(null);
        } else {
            SkinPack pack = skinPackRepository.findById(skinPackId)
                    .orElseThrow(() -> new IllegalArgumentException("Pacote de skin não encontrado"));
            user.setEquippedSkinPack(pack);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public EquippedSkinResponse getEquipped(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        SkinPack pack = user.getEquippedSkinPack();

        if (pack == null) {
            return new EquippedSkinResponse(null, null, null);
        }

        return new EquippedSkinResponse(pack.getId(), pack.getSlug(), pack.getName());
    }

    @Transactional(readOnly = true)
    public String getSkinSlug(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        SkinPack pack = user.getEquippedSkinPack();
        return pack != null ? pack.getSlug() : null;
    }

    private SkinPackResponse toResponse(SkinPack pack) {
        return new SkinPackResponse(pack.getId(), pack.getSlug(), pack.getName(), pack.getDescription());
    }
}
