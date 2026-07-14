package com.projeto.navalstrikeAPI.domain.skin.entity;

import com.projeto.navalstrikeAPI.common.enums.ShipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "skin_assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"skin_pack_id", "ship_type"})
})
@Getter
@Setter
@NoArgsConstructor
public class SkinAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_pack_id", nullable = false)
    private SkinPack skinPack;

    @Enumerated(EnumType.STRING)
    @Column(name = "ship_type", nullable = false, length = 50)
    private ShipType shipType;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}
