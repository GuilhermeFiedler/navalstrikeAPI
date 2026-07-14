package com.projeto.navalstrikeAPI.domain.skin.repository;

import com.projeto.navalstrikeAPI.domain.skin.entity.SkinAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SkinAssetRepository extends JpaRepository<SkinAsset, UUID> {
}
