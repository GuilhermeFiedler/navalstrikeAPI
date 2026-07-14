package com.projeto.navalstrikeAPI.domain.skin.repository;

import com.projeto.navalstrikeAPI.domain.skin.entity.SkinPack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkinPackRepository extends JpaRepository<SkinPack, UUID> {
    Optional<SkinPack> findBySlug(String slug);
}
