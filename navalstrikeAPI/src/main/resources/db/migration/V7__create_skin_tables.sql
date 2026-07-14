CREATE TABLE skin_packs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE skin_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skin_pack_id UUID NOT NULL REFERENCES skin_packs(id),
    ship_type VARCHAR(50) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    UNIQUE(skin_pack_id, ship_type)
);

ALTER TABLE users ADD COLUMN equipped_skin_pack_id UUID REFERENCES skin_packs(id);
