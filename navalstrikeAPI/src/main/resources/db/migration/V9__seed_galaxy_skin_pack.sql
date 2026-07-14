INSERT INTO skin_packs (id, slug, name, description, created_at)
VALUES (
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'galaxy',
    'Galaxy',
    'Navios inspirados em naves espaciais',
    now()
) ON CONFLICT (slug) DO NOTHING;

INSERT INTO skin_assets (id, skin_pack_id, ship_type, image_url) VALUES
    (gen_random_uuid(), 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'CARRIER', 'galaxycarrier.png'),
    (gen_random_uuid(), 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'BATTLESHIP', 'galaxybattleship.png'),
    (gen_random_uuid(), 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'CRUISER', 'galaxycruiser.png'),
    (gen_random_uuid(), 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'SUBMARINE', 'galaxysubmarine.png'),
    (gen_random_uuid(), 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'DESTROYER', 'galaxydestroyer.png')
ON CONFLICT (skin_pack_id, ship_type) DO NOTHING;
